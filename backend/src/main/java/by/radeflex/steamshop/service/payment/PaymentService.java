package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.event.payment.CreateOrderEvent;
import by.radeflex.steamshop.event.payment.ProcessOrderEvent;
import by.radeflex.steamshop.props.ShopProperties;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.dynomake.yookassa.Yookassa;
import me.dynomake.yookassa.exception.BadRequestException;
import me.dynomake.yookassa.exception.UnspecifiedShopInformation;
import me.dynomake.yookassa.model.Amount;
import me.dynomake.yookassa.model.Confirmation;
import me.dynomake.yookassa.model.Payment;
import me.dynomake.yookassa.model.request.PaymentRequest;
import me.dynomake.yookassa.model.request.RefundRequest;
import me.dynomake.yookassa.model.request.receipt.Receipt;
import me.dynomake.yookassa.model.request.receipt.ReceiptCustomer;
import me.dynomake.yookassa.model.request.receipt.ReceiptItem;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final Yookassa yookassa;
    private final PaymentRepository paymentRepository;
    private final ShopProperties shopProperties;
    private final PaymentItemRepository paymentItemRepository;
    private final AccountService accountService;
    private final ApplicationEventPublisher publisher;

    @Caching(evict = {
            @CacheEvict(
                    value = "user::product-history",
                    key = "#result.user.id",
                    condition = "#result != null"),
            @CacheEvict(
                    value = "user::current",
                    key = "#result.user.id",
                    condition = "#result != null"),
            @CacheEvict(
                    value = "products",
                    allEntries = true,
                    condition = "#result != null"),
            @CacheEvict(
                    value = "cart",
                    allEntries = true,
                    condition = "#result != null"
            )
    })
    @Transactional
    public by.radeflex.steamshop.entity.Payment handleNotification(PaymentStatusDto dto) {
        var payment = paymentRepository.findById(dto.id());
        return payment.map(p -> {
            switch (p.getType()) {
                case TOP_UP -> processTopUp(dto, p);
                case PURCHASE -> processPurchase(dto, p);
            }
            p.setStatus(dto.event());
            paymentRepository.save(p);
            if (dto.event() != PaymentStatus.WAITING_FOR_CAPTURE)
                publisher.publishEvent(new CreateOrderEvent(this, p));
            return p;
        }).orElseThrow();
    }

    public void createRefund(by.radeflex.steamshop.entity.Payment p) throws UnspecifiedShopInformation, BadRequestException, IOException {
        yookassa.createRefund(RefundRequest.builder()
                .paymentId(p.getId())
                .amount(new Amount(p.getAmount() + ".00", "RUB"))
                .build());
    }

    @SneakyThrows
    by.radeflex.steamshop.entity.Payment createPaymentTopUp(UUID key, Integer amount, User user) {
        var up = UserProduct.builder()
                .user(user)
                .product(Product.builder()
                        .title("Пополнение "+user.getUsername())
                        .price(amount)
                        .build())
                .quantity(1)
                .build();
        var payment = createYookassaPayment(amount, List.of(up), user);
        return savePayment(payment, user, PaymentType.TOP_UP, null, key);
    }

    Optional<by.radeflex.steamshop.entity.Payment> findPaymentByKey(UUID key) {
        return paymentRepository.findByIdempotencyKey(key);
    }

     by.radeflex.steamshop.entity.Payment createPaymentViaBalance(UUID key, User u, Product p) {
        var ePayment = paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(UUID.randomUUID())
                .user(u)
                .source(PaymentSource.CLICK)
                .type(PaymentType.PURCHASE)
                .amount(p.getPrice())
                .status(PaymentStatus.SUCCEEDED)
                .idempotencyKey(key)
                .build());
        var up = List.of(new UserProduct(null, u, p, 1));
        savePaymentItems(up, ePayment);
        return ePayment;
    }

    by.radeflex.steamshop.entity.Payment createPaymentCartViaBalance(UUID key, User user, Integer sum, List<UserProduct> cart) {
        var ePayment = paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(UUID.randomUUID())
                .user(user)
                .source(PaymentSource.CART)
                .type(PaymentType.PURCHASE)
                .amount(sum)
                .status(PaymentStatus.SUCCEEDED)
                .idempotencyKey(key)
                .build());
        savePaymentItems(cart, ePayment);
        return ePayment;
    }

    @SneakyThrows
    by.radeflex.steamshop.entity.Payment createPaymentViaCard(UUID key, Integer sum, User user, PaymentSource source, List<UserProduct> cart) {
        var payment = createYookassaPayment(sum, cart, user);
        var ePayment = savePayment(payment, user, PaymentType.PURCHASE, source, key);
        savePaymentItems(cart, ePayment);
        return ePayment;
    }

    private void processTopUp(PaymentStatusDto dto, by.radeflex.steamshop.entity.Payment p) {
        if (dto.event() == PaymentStatus.SUCCEEDED)
            p.getUser().topUp(p.getAmount());
    }

    private void processPurchase(PaymentStatusDto dto, by.radeflex.steamshop.entity.Payment p) {
        switch (dto.event()) {
            case CANCELED -> accountService.unreserve(p);
            case SUCCEEDED -> publisher.publishEvent(new ProcessOrderEvent(this, p));
        }
    }

    private Payment createYookassaPayment(Integer sum, List<UserProduct> cart, User user) throws UnspecifiedShopInformation, BadRequestException, IOException {
        return yookassa.createPayment(PaymentRequest.builder()
                .amount(new Amount(sum + ".00", "RUB"))
                .description("Покупка " + cart.size() + " товаров на 812shop.org")
                .confirmation(Confirmation.builder()
                        .type("redirect")
                        .returnUrl(shopProperties.getReturnUrl())
                        .build())
                .savePaymentMethod(true)
                .receipt(Receipt.builder()
                        .customer(ReceiptCustomer.builder()
                                .email(user.getEmail())
                                .build())
                        .items(cart.stream()
                                .map(up -> ReceiptItem.builder()
                                        .amount(new Amount(
                                                up.getProduct().getPrice() * up.getQuantity() + ".00",
                                                "RUB"))
                                        .subject("service")
                                        .quantity(up.getQuantity())
                                        .vat(1)
                                        .paymentMode("full_payment")
                                        .description(up.getProduct().getTitle())
                                        .build()).toList())
                        .build())
                .build());
    }

    private List<PaymentItem> savePaymentItems(List<UserProduct> cart,  by.radeflex.steamshop.entity.Payment ePayment) {
        var pis = cart.stream()
                .map(up -> paymentItemRepository.save(PaymentItem.builder()
                            .product(up.getProduct())
                            .payment(ePayment)
                            .quantity(up.getQuantity())
                            .build())).toList();
        accountService.reserve(ePayment);
        return pis;
    }

    private by.radeflex.steamshop.entity.Payment savePayment(Payment payment, User user, PaymentType type, PaymentSource source, UUID key) {
        return paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(payment.getId())
                .source(source)
                .confirmationUrl(payment.getConfirmation().getConfirmationUrl())
                .type(type)
                .status(PaymentStatus.valueOf(payment.getStatus().toUpperCase()))
                .amount(Double.valueOf(payment.getAmount().getValue()).intValue())
                .user(user)
                .idempotencyKey(key)
                .build());
    }
}
