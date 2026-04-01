package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.entity.PaymentStatus;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {
    private final int USER_ID = 2;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    void topUp_shouldWork() {
        var u = User.builder().id(USER_ID).balance(100).build();
        var oldBalance = u.getBalance();
        var topUpDto = new TopUpDto(1000);
        var payment = Payment.builder()
                .id(UUID.randomUUID())
                .status(PaymentStatus.PENDING)
                .confirmationUrl("confirmation.url").build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(paymentService.createPaymentTopUp(topUpDto.amount(), u)).thenReturn(payment);

        balanceService.topUp(topUpDto);
        verify(paymentService, times(1)).createPaymentTopUp(topUpDto.amount(), u);
        verify(notificationService, times(1)).sendPayment(payment);
        assertEquals(u.getBalance(), oldBalance);
    }
}
