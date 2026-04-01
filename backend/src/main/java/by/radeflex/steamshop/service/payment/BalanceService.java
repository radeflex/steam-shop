package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @Transactional
    public String topUp(TopUpDto topUpDto) {
        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow();
        var ePayment = paymentService.createPaymentTopUp(topUpDto.amount(), user);
        notificationService.sendPayment(ePayment);
        return ePayment.getConfirmationUrl();
    }
}
