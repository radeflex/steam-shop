package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.PurchaseCreateDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.mapper.ProductHistoryMapper;
import by.radeflex.steamshop.props.ShopProperties;
import by.radeflex.steamshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import java.util.UUID;

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
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final ProductHistoryMapper productHistoryMapper;

    @SneakyThrows
    public String topUp(TopUpDto topUpDto) {
        User user = userRepository.findById(AuthService.getCurrentUser().getId())
                .orElseThrow();
        Amount amount = new Amount(topUpDto.amount()+"", "RUB");
        Payment payment = yookassa.createPayment(PaymentRequest.builder()
                .amount(amount)
                        .confirmation(Confirmation.builder()
                                .type("redirect")
                                .returnUrl(shopProperties.getReturnUrl())
                                .build())
                        .savePaymentMethod(true)
                        .receipt(Receipt.builder()
                                .customer(ReceiptCustomer.builder()
                                        .email(user.getEmail())
                                        .build())
                                .items(List.of(ReceiptItem.builder()
                                                .description("Пополнение "+user.getUsername())
                                                .subject("service")
                                                .vat(1)
                                                .amount(amount)
                                                .quantity(1)
                                                .paymentMode("full_payment")
                                        .build()))
                                .build())
                        .description("Пополнение "+user.getUsername())
                .build());
        var ePayment = savePayment(payment, user, PaymentType.TOP_UP);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }

    @SneakyThrows
    public void handleNotification(PaymentStatusDto dto) {
        var payment = paymentRepository.findById(dto.id());
        try {
            payment.map(p -> {
                switch (p.getType()) {
                    case TOP_UP -> processTopUp(dto, p);
                    case PURCHASE -> processPurchase(dto, p);
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

    private void processTopUp(PaymentStatusDto dto, by.radeflex.steamshop.entity.Payment p) {
        switch (dto.event()) {
            case CANCELLED -> p.setStatus(PaymentStatus.CANCELLED);
            case SUCCEEDED -> {
                p.getUser().topUp(p.getAmount().intValue());
                p.setStatus(PaymentStatus.SUCCEEDED);
            }
        }
        paymentRepository.save(p);
    }

    private void processPurchase(PaymentStatusDto dto, by.radeflex.steamshop.entity.Payment p) {
        switch (dto.event()) {
            case CANCELLED -> {
                accountService.unreserve(p);
                p.setStatus(PaymentStatus.CANCELLED);
            }
            case SUCCEEDED -> {
                var accounts = accountService.sellAccounts(p);
                p.setStatus(PaymentStatus.SUCCEEDED);
                mailService.sendAccounts(p, accounts);
                saveHistory(paymentItemRepository.findAllByPayment(p));
            }
        }
        paymentRepository.save(p);
    }

    private void saveHistory(List<PaymentItem> paymentItemRepository) {
        paymentItemRepository.stream()
                .map(productHistoryMapper::mapFrom)
                .forEach(userProductHistoryRepository::save);
    }

    public boolean purchaseViaBalance(PurchaseCreateDto dto) {
        User user = userRepository.findById(AuthService.getCurrentUser().getId())
                .orElseThrow();
        var products = productRepository.findByIdIn(dto.products().keySet());
        if (products.size() != dto.products().size())
            throw new IllegalArgumentException("Invalid product ids");

        Double sum = products.stream()
                .mapToDouble(p -> p.getPrice() * dto.products().get(p.getId()))
                .sum();
        if (!user.withdraw(sum.intValue())) {
            return false;
        }
        var ePayment = paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(UUID.randomUUID())
                .user(user)
                .type(PaymentType.PURCHASE)
                .amount(sum)
                .status(PaymentStatus.SUCCEEDED)
                .build());
        List<PaymentItem> items = savePaymentItems(dto, products, ePayment);
        saveHistory(items);
        var accounts = accountService.sellAccounts(ePayment);
        notificationService.sendPayment(ePayment);
        mailService.sendAccounts(ePayment, accounts);
        return true;
    }

    @SneakyThrows
    public String purchaseViaCard(PurchaseCreateDto dto) {
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
        var ePayment = savePayment(payment, user, PaymentType.PURCHASE);
        savePaymentItems(dto, products, ePayment);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }

    private List<PaymentItem> savePaymentItems(PurchaseCreateDto dto, List<Product> products, by.radeflex.steamshop.entity.Payment ePayment) {
        return products.stream()
                .map(p -> PaymentItem.builder()
                            .product(p)
                            .payment(ePayment)
                            .quantity(dto.products().get(p.getId()))
                            .build())
                .peek(p -> {
            paymentItemRepository.save(p);
            accountService.reserve(ePayment);
        }).toList();
    }

    private by.radeflex.steamshop.entity.Payment savePayment(Payment payment, User user, PaymentType type) {
        return paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(payment.getId())
                .confirmationUrl(payment.getConfirmation().getConfirmationUrl())
                .type(type)
                .status(PaymentStatus.valueOf(payment.getStatus().toUpperCase()))
                .amount(Double.valueOf(payment.getAmount().getValue()))
                .user(user)
                .build());
    }
}
