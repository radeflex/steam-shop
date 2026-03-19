package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.mapper.ProductHistoryMapper;
import by.radeflex.steamshop.props.ShopProperties;
import by.radeflex.steamshop.repository.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    private final Yookassa yookassa;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ShopProperties shopProperties;
    private final NotificationService notificationService;
    private final PaymentItemRepository paymentItemRepository;
    private final MailService mailService;
    private final AccountService accountService;
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final ProductHistoryMapper productHistoryMapper;
    private final UserProductRepository userProductRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    @SneakyThrows
    public String topUp(TopUpDto topUpDto) {
        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow();
        int sum = topUpDto.amount();
        var up = UserProduct.builder()
                .user(user)
                .product(Product.builder()
                        .title("Пополнение "+user.getUsername())
                        .price(sum)
                        .build())
                .quantity(1)
                .build();
        Payment payment = buildPayment((double)sum, List.of(up), user);
        var ePayment = savePayment(payment, user, PaymentType.TOP_UP, null);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }

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
    @SneakyThrows
    public by.radeflex.steamshop.entity.Payment handleNotification(PaymentStatusDto dto) {
        var payment = paymentRepository.findById(dto.id());
        try {
            return payment.map(p -> {
                switch (p.getType()) {
                    case TOP_UP -> processTopUp(dto, p);
                    case PURCHASE -> processPurchase(dto, p);
                }
                if (dto.event() != PaymentStatus.WAITING_FOR_CAPTURE)
                    notificationService.sendPayment(p);
                return p;
            }).orElseThrow();
        } catch (AccountLackException e) {
            yookassa.createRefund(RefundRequest.builder()
                            .paymentId(dto.id())
                            .amount(new Amount(payment.get().getAmount()+"", "RUB"))
                    .build());
            return null;
        }
    }

    private void processTopUp(PaymentStatusDto dto, by.radeflex.steamshop.entity.Payment p) {
        switch (dto.event()) {
            case CANCELED -> p.setStatus(PaymentStatus.CANCELED);
            case SUCCEEDED -> {
                p.getUser().topUp(p.getAmount().intValue());
                p.setStatus(PaymentStatus.SUCCEEDED);
            }
        }
        paymentRepository.save(p);
    }

    private void processPurchase(PaymentStatusDto dto, by.radeflex.steamshop.entity.Payment p) {
        switch (dto.event()) {
            case CANCELED -> accountService.unreserve(p);
            case SUCCEEDED -> {
                var accounts = accountService.sellAccounts(p);
                mailService.sendAccounts(p, accounts);
                saveHistory(paymentItemRepository.findAllByPayment(p));
                if (p.getSource() == PaymentSource.CART)
                    userProductRepository.deleteAllByUser(p.getUser());
            }
        }
        p.setStatus(dto.event());
        paymentRepository.save(p);
    }

    private void saveHistory(List<PaymentItem> paymentItemRepository) {
        paymentItemRepository.stream()
                .map(productHistoryMapper::mapFrom)
                .forEach(userProductHistoryRepository::save);
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "user::current",
                    key = "@currentUserService.getCurrentUserId()",
                    condition = "#result"),
            @CacheEvict(
                    value = "user::product-history",
                    key = "@currentUserService.getCurrentUserId()",
                    condition = "#result"),
            @CacheEvict(
                    value = "products",
                    allEntries = true,
                    condition = "#result"),
            @CacheEvict(
                    value = "cart",
                    allEntries = true,
                    condition = "#result"
            )})
    public boolean purchaseCartViaBalance() {
        var user = userRepository.findById(currentUserService.getCurrentUserId()).orElseThrow();
        var cart = userProductRepository.findAvailableByUser(user);
        if (cart.isEmpty()) throw new IllegalArgumentException();
        Double sum = cart.stream()
                .mapToDouble(up -> up.getProduct().getPrice() * up.getQuantity())
                .sum();
        if (!user.withdraw(sum.intValue())) {
            return false;
        }

        var ePayment = paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(UUID.randomUUID())
                .user(user)
                .source(PaymentSource.CART)
                .type(PaymentType.PURCHASE)
                .amount(sum)
                .status(PaymentStatus.SUCCEEDED)
                .build());
        List<PaymentItem> items = savePaymentItems(cart, ePayment);
        saveHistory(items);
        var accounts = accountService.sellAccounts(ePayment);
        notificationService.sendPayment(ePayment);
        mailService.sendAccounts(ePayment, accounts);
        userProductRepository.deleteAllByUser(user);
        return true;
    }

    @Caching(evict = {
            @CacheEvict(
                value = "products",
                allEntries = true,
                condition = "#result != null"),
            @CacheEvict(
                value = "cart",
                allEntries = true,
                condition = "#result != null")})
    @SneakyThrows
    public String purchaseCartViaCard() {
        var user = userRepository.findById(currentUserService.getCurrentUserId()).orElseThrow();
        var cart = userProductRepository.findAvailableByUser(user);
        if (cart.isEmpty()) throw new IllegalArgumentException();
        Double sum = cart.stream()
                .mapToDouble(up -> up.getProduct().getPrice() * up.getQuantity())
                .sum();

        Payment payment = buildPayment(sum, cart, user);
        var ePayment = savePayment(payment, user, PaymentType.PURCHASE, PaymentSource.CART);
        savePaymentItems(cart, ePayment);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }

    private Payment buildPayment(Double sum, List<UserProduct> cart, User user) throws UnspecifiedShopInformation, BadRequestException, IOException {
        return yookassa.createPayment(PaymentRequest.builder()
                .amount(new Amount(String.valueOf(sum), "RUB"))
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
        return cart.stream()
                .map(up -> PaymentItem.builder()
                            .product(up.getProduct())
                            .payment(ePayment)
                            .quantity(up.getQuantity())
                            .build())
                .peek(pi -> {
            paymentItemRepository.save(pi);
            accountService.reserve(pi.getPayment());
        }).toList();
    }

    private by.radeflex.steamshop.entity.Payment savePayment(Payment payment, User user, PaymentType type, PaymentSource source) {
        return paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(payment.getId())
                .source(source)
                .confirmationUrl(payment.getConfirmation().getConfirmationUrl())
                .type(type)
                .status(PaymentStatus.valueOf(payment.getStatus().toUpperCase()))
                .amount(Double.valueOf(payment.getAmount().getValue()))
                .user(user)
                .build());
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "user::current",
                    key = "@currentUserService.getCurrentUserId()",
                    condition = "#result"),
            @CacheEvict(
                    value = "user::product-history",
                    key = "@currentUserService.getCurrentUserId()",
                    condition = "#result"),
            @CacheEvict(
                    value = "products",
                    allEntries = true,
                    condition = "#result"),
            @CacheEvict(
                    value = "cart",
                    allEntries = true,
                    condition = "#result"
            )})
    public boolean purchaseViaBalance(Integer productId) {
        var u = userRepository.findById(currentUserService.getCurrentUserId()).orElseThrow();
        var p = productRepository.findById(productId);
        if (p.isEmpty() || !u.withdraw(p.get().getPrice()))
            return false;
        var ePayment = paymentRepository.save(by.radeflex.steamshop.entity.Payment.builder()
                .id(UUID.randomUUID())
                .user(u)
                .source(PaymentSource.CLICK)
                .type(PaymentType.PURCHASE)
                .amount(p.get().getPrice().doubleValue())
                .status(PaymentStatus.SUCCEEDED)
                .build());
        var up = List.of(new UserProduct(null, u, p.get(), 1));
        List<PaymentItem> items = savePaymentItems(up, ePayment);
        saveHistory(items);
        var accounts = accountService.sellAccounts(ePayment);
        notificationService.sendPayment(ePayment);
        mailService.sendAccounts(ePayment, accounts);
        return true;
    }

    @Caching(evict = {
            @CacheEvict(
                    value = "products",
                    allEntries = true,
                    condition = "#result.isPresent()"),
            @CacheEvict(
                    value = "cart",
                    allEntries = true,
                    condition = "#result.isPresent()"
            )})
    @SneakyThrows
    public Optional<String> purchaseViaCard(Integer productId) {
        var u = currentUserService.getCurrentUserEntity();
        var p = productRepository.findById(productId);
        if (p.isEmpty())
            return Optional.empty();
        var up = List.of(new UserProduct(null, u, p.get(), 1));
        Payment payment = buildPayment(p.get().getPrice().doubleValue(), up, u);
        var ePayment = savePayment(payment, u, PaymentType.PURCHASE, PaymentSource.CLICK);
        savePaymentItems(up, ePayment);
        notificationService.sendPayment(ePayment);
        return Optional.of(payment.getConfirmation().getConfirmationUrl());
    }
}
