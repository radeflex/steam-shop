package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.PurchaseCreateDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.props.ShopProperties;
import by.radeflex.steamshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.dynomake.yookassa.Yookassa;
import me.dynomake.yookassa.model.Amount;
import me.dynomake.yookassa.model.Confirmation;
import me.dynomake.yookassa.model.Payment;
import me.dynomake.yookassa.model.request.PaymentRequest;
import me.dynomake.yookassa.model.request.RefundRequest;
import me.dynomake.yookassa.model.request.receipt.Receipt;
import me.dynomake.yookassa.model.request.receipt.ReceiptCustomer;
import me.dynomake.yookassa.model.request.receipt.ReceiptItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final Yookassa yookassa;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ShopProperties shopProperties;
    private final NotificationService notificationService;
    private final PaymentItemRepository paymentItemRepository;
    private final MailService mailService;
    private final AccountService accountService;

    @SneakyThrows
    public void handleNotification(PaymentStatusDto dto) {
        var payment = paymentRepository.findById(dto.id());
        try {
            payment.map(p -> {
                switch (dto.event()) {
                    case CANCELLED -> {
                        accountService.unreserve(p);
                        p.setStatus(PaymentStatus.CANCELLED);
                        paymentRepository.save(p);
                    }
                    case SUCCEEDED -> {
                        var accounts = accountService.sellAccounts(p);
                        p.setStatus(PaymentStatus.SUCCEEDED);
                        paymentRepository.save(p);
                        mailService.sendAccounts(p, accounts);
                    }
                }
                if (dto.event() != PaymentStatus.WAITING_FOR_CAPTURE)
                    notificationService.sendPayment(p);
                return true;
            });
        } catch (AccountLackException e) {
            yookassa.createRefund(RefundRequest.builder()
                            .paymentId(dto.id())
                            .amount(new Amount(payment.get().getAmount()+"", "RUB"))
                    .build());
        }
    }

    @SneakyThrows
    public String purchase(PurchaseCreateDto dto) {
        User user = userRepository.findById(AuthService.getCurrentUser().getId())
                .orElseThrow();
        var products = productRepository.findByIdIn(dto.products().keySet());
        if (products.size() != dto.products().size())
            throw new IllegalArgumentException("Invalid product ids");

        Double sum = products.stream()
                .mapToDouble(p -> p.getPrice() * dto.products().get(p.getId()))
                .sum();

        Payment payment = yookassa.createPayment(PaymentRequest.builder()
                .amount(new Amount(String.valueOf(sum), "RUB"))
                .description("Покупка " + dto.products().size() + " товаров на 812shop.org")
                .confirmation(Confirmation.builder()
                        .type("redirect")
                        .returnUrl(shopProperties.getReturnUrl())
                        .build())
                .savePaymentMethod(true)
                .receipt(Receipt.builder()
                        .customer(ReceiptCustomer.builder()
                                .email(user.getEmail())
                                .build())
                        .items(products.stream()
                                .map(p -> ReceiptItem.builder()
                                        .amount(new Amount(
                                                p.getPrice()*dto.products().get(p.getId())+".00",
                                                "RUB"))
                                        .subject("service")
                                        .quantity(dto.products().get(p.getId()))
                                        .vat(1)
                                        .paymentMode("full_payment")
                                        .description(p.getTitle())
                                        .build()).toList())
                        .build())
                .build());
        log.info(payment.getStatus());
        var ePayment = savePayment(payment, user);
        savePaymentItems(dto, products, ePayment);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }

    private void savePaymentItems(PurchaseCreateDto dto, List<Product> products, by.radeflex.steamshop.entity.Payment ePayment) {
        products.forEach(p -> {
            paymentItemRepository.save(PaymentItem.builder()
                            .product(p)
                            .payment(ePayment)
                            .quantity(dto.products().get(p.getId()))
                    .build());
        });
        accountService.reserve(ePayment);
    }

    private by.radeflex.steamshop.entity.Payment savePayment(Payment payment, User user) {
        return paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(payment.getId())
                .confirmationUrl(payment.getConfirmation().getConfirmationUrl())
                .status(PaymentStatus.valueOf(payment.getStatus().toUpperCase()))
                .amount(Double.valueOf(payment.getAmount().getValue()))
                .user(user)
                .build());
    }
}
