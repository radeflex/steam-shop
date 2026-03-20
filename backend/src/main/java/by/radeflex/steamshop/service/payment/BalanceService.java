package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.dynomake.yookassa.model.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @SneakyThrows
    @Transactional
    public String topUp(TopUpDto topUpDto) {
        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow();
        var amount = topUpDto.amount();
        var up = UserProduct.builder()
                .user(user)
                .product(Product.builder()
                        .title("Пополнение "+user.getUsername())
                        .price(amount)
                        .build())
                .quantity(1)
                .build();
        Payment payment = paymentService.createYookassaPayment(amount, List.of(up), user);
        var ePayment = paymentService.createPaymentTopUp(payment, user);
        notificationService.sendPayment(ePayment);
        return payment.getConfirmation().getConfirmationUrl();
    }
}
