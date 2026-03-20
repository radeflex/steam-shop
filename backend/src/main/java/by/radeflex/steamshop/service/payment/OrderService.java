package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.entity.PaymentSource;
import by.radeflex.steamshop.entity.UserProduct;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductRepository;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.MailService;
import by.radeflex.steamshop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.dynomake.yookassa.model.Payment;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final AccountService accountService;
    private final CurrentUserService currentUserService;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final UserProductRepository userProductRepository;

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
        var ePayment = paymentService.createPaymentViaBalance(u, p.get());
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
        Payment payment = paymentService.createYookassaPayment(p.get().getPrice(), up, u);
        var ePayment = paymentService.createPaymentCartViaCard(payment, u, PaymentSource.CLICK, up);
        notificationService.sendPayment(ePayment);
        return Optional.of(payment.getConfirmation().getConfirmationUrl());
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
        Integer sum = cart.stream().mapToInt(up ->
                        up.getProduct().getPrice() * up.getQuantity()).sum();
        if (!user.withdraw(sum)) {
            return false;
        }

        var ePayment = paymentService.createPaymentCartViaBalance(user, sum, cart);
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
        Integer sum = cart.stream().mapToInt(up ->
                up.getProduct().getPrice() * up.getQuantity()).sum();

        Payment payment = paymentService.createYookassaPayment(sum, cart, user);
        var ePayment = paymentService.createPaymentCartViaCard(payment, user, PaymentSource.CART, cart);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }
}
