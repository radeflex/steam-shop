package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.mapper.ProductHistoryMapper;
import by.radeflex.steamshop.props.ShopProperties;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.PaymentRepository;
import by.radeflex.steamshop.repository.UserProductHistoryRepository;
import by.radeflex.steamshop.repository.UserProductRepository;
import by.radeflex.steamshop.service.MailService;
import by.radeflex.steamshop.service.NotificationService;
import me.dynomake.yookassa.Yookassa;
import me.dynomake.yookassa.exception.BadRequestException;
import me.dynomake.yookassa.exception.UnspecifiedShopInformation;
import me.dynomake.yookassa.model.Amount;
import me.dynomake.yookassa.model.Confirmation;
import me.dynomake.yookassa.model.request.PaymentRequest;
import me.dynomake.yookassa.model.request.RefundRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private final int PRODUCT_ID = 2;
    private final int USER_ID = 3;
    @Mock
    private Yookassa yookassa;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ShopProperties shopProperties;
    @Mock
    private NotificationService notificationService;
    @Mock
    private PaymentItemRepository paymentItemRepository;
    @Mock
    private MailService mailService;
    @Mock
    private AccountService accountService;
    @Mock
    private UserProductHistoryRepository userProductHistoryRepository;
    @Spy
    private ProductHistoryMapper productHistoryMapper;
    @Mock
    private UserProductRepository userProductRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPaymentCartViaBalance_shouldWork() {
        List<UserProduct> cart = new ArrayList<>();
        var u = User.builder().id(USER_ID).build();
        int sum = 0;
        for (int i = 1; i < 5; i++) {
            var p = Product.builder()
                    .id(i + 1)
                    .price(i * 10).build();
            cart.add(new UserProduct(i, u, p, 1));
            sum += i * 10;
        }

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(paymentItemRepository.save(any(PaymentItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        
        var result = paymentService.createPaymentCartViaBalance(u, sum, cart);
        int finalSum = sum;
        // saves payment properly
        verify(paymentRepository).save(argThat(p ->
                p.getSource().equals(PaymentSource.CART)
                && p.getUser().equals(u)
                && p.getAmount().equals(finalSum)
                && p.getType().equals(PaymentType.PURCHASE)
                && p.getStatus().equals(PaymentStatus.SUCCEEDED)));
        // saves items properly n times
        verify(paymentItemRepository, times(cart.size())).save(
                argThat(pi -> pi.getPayment().equals(result)));
        // saves history n times
        verify(userProductHistoryRepository, times(cart.size())).save(
                argThat(pi -> pi.getPayment().equals(result)));
        // reserves accounts exactly one time
        verify(accountService, times(1)).reserve(eq(result));
    }

    @Test
    void createPaymentViaBalance_shouldWork() {
        var p = Product.builder().id(PRODUCT_ID).price(123).build();
        var u = User.builder().id(USER_ID).build();

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(paymentItemRepository.save(any(PaymentItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = paymentService.createPaymentViaBalance(u, p);
        // saves payment item exactly 1 time
        verify(paymentItemRepository, times(1)).save(argThat(pi ->
                pi.getPayment().equals(result)
                && pi.getQuantity().equals(1)
                && pi.getProduct().equals(p)));
        // saves payment properly
        verify(paymentRepository).save(argThat(pm ->
                pm.getUser().equals(u)
                && pm.getType().equals(PaymentType.PURCHASE)
                && pm.getStatus().equals(PaymentStatus.SUCCEEDED)
                && pm.getSource().equals(PaymentSource.CLICK)
                && pm.getAmount().equals(p.getPrice())));
        // saves history properly
        verify(userProductHistoryRepository, times(1)).save(argThat(uph ->
                uph.getPayment().equals(result)
                && uph.getProduct().equals(p)
                && uph.getQuantity().equals(1)
                && uph.getUser().equals(u)));
        // reserves account only once
        verify(accountService, times(1)).reserve(result);
    }

    @Test
    void createPaymentViaCard_shouldWorkAndReserve() throws UnspecifiedShopInformation, BadRequestException, IOException {
        List<UserProduct> cart = new ArrayList<>();
        var u = User.builder().id(USER_ID).build();
        int sum = 0;
        for (int i = 1; i < 5; i++) {
            var p = Product.builder()
                    .id(i + 1)
                    .price(i * 10).build();
            cart.add(new UserProduct(i, u, p, 1));
            sum += i * 10;
        }

        var pm = mock(me.dynomake.yookassa.model.Payment.class);

        when(pm.getId()).thenReturn(UUID.randomUUID());
        when(pm.getAmount()).thenReturn(new Amount(sum + ".00", "RUB"));
        when(pm.getStatus()).thenReturn("pending");
        when(pm.getConfirmation()).thenReturn(new Confirmation("type", "locale", "return-url", "confirmation-url"));

        when(yookassa.createPayment(any(PaymentRequest.class)))
                .thenReturn(pm);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(paymentItemRepository.save(any(PaymentItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = paymentService.createPaymentViaCard(sum, u, PaymentSource.CART, cart);
        int finalSum = sum;
        verify(yookassa, times(1)).createPayment(any(PaymentRequest.class));
        verify(paymentRepository).save(argThat(p ->
                p.getStatus().equals(PaymentStatus.PENDING)
                && p.getId().equals(pm.getId())
                && p.getConfirmationUrl().equals(pm.getConfirmation().getConfirmationUrl())
                && p.getUser().equals(u)
                && p.getAmount().equals(finalSum)
                && p.getSource().equals(PaymentSource.CART)
                && p.getType().equals(PaymentType.PURCHASE)));
        verify(paymentItemRepository, times(cart.size())).save(argThat(pi ->
                pi.getPayment().equals(result)));
        verify(accountService, times(1)).reserve(result);
    }

    @Test
    void createPaymentTopUp_shouldWork() throws UnspecifiedShopInformation, BadRequestException, IOException {
        var dto = new TopUpDto(100);
        var u = User.builder().id(USER_ID).build();
        var pm = mock(me.dynomake.yookassa.model.Payment.class);

        when(pm.getId()).thenReturn(UUID.randomUUID());
        when(pm.getAmount()).thenReturn(new Amount(dto.amount() + ".00", "RUB"));
        when(pm.getStatus()).thenReturn("pending");
        when(pm.getConfirmation()).thenReturn(new Confirmation("type", "locale", "return-url", "confirmation-url"));
        when(yookassa.createPayment(any(PaymentRequest.class)))
                .thenReturn(pm);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        paymentService.createPaymentTopUp(dto.amount(), u);
        verify(yookassa, times(1)).createPayment(any(PaymentRequest.class));
        verify(paymentRepository).save(argThat(p ->
                p.getStatus().equals(PaymentStatus.PENDING)
                && p.getId().equals(pm.getId())
                && p.getUser().equals(u)
                && p.getConfirmationUrl().equals(pm.getConfirmation().getConfirmationUrl())
                && p.getAmount().equals(dto.amount())
                && p.getType().equals(PaymentType.TOP_UP)
                && p.getSource() == null));
    }

    @Test
    void handleNotification_shouldWork_whenPurchaseSucceed() {
        var u = User.builder().id(USER_ID).build();
        var dto = new PaymentStatusDto(UUID.randomUUID(), PaymentStatus.SUCCEEDED, "conf-url");
        var payment = Payment.builder()
                        .id(dto.id())
                        .status(dto.event())
                        .source(PaymentSource.CART)
                        .type(PaymentType.PURCHASE)
                        .user(u)
                        .confirmationUrl(dto.url()).build();
        when(paymentRepository.findById(dto.id())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        paymentService.handleNotification(dto);
        verify(paymentRepository).save(argThat(p -> p.getId().equals(dto.id())
                && p.getStatus().equals(dto.event())));
        verify(accountService, times(1)).sellAccounts(payment);
        verify(notificationService, times(1)).sendPayment(payment);
        verify(mailService, times(1)).sendAccounts(eq(payment), any());
        verify(userProductRepository, times(1)).deleteAllByUser(payment.getUser());
    }

    @Test
    void handleNotification_shouldUnreserve_whenPurchaseCanceled() {
        var u = User.builder().id(USER_ID).build();
        var dto = new PaymentStatusDto(UUID.randomUUID(), PaymentStatus.CANCELED, "conf-url");
        var payment = Payment.builder()
                .id(dto.id())
                .status(dto.event())
                .source(PaymentSource.CART)
                .type(PaymentType.PURCHASE)
                .user(u)
                .confirmationUrl(dto.url()).build();
        when(paymentRepository.findById(dto.id())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        paymentService.handleNotification(dto);
        verify(paymentRepository).save(argThat(p -> p.getId().equals(dto.id())
                && p.getStatus().equals(dto.event())));
        verify(accountService, times(1)).unreserve(payment);
        verify(accountService, never()).sellAccounts(payment);
        verify(notificationService, times(1)).sendPayment(payment);
        verify(mailService, never()).sendAccounts(eq(payment), any());
        verify(userProductRepository, never()).deleteAllByUser(payment.getUser());
    }

    @Test
    void handleNotification_shouldTopUp_whenPurchaseSucceed() {
        var u = User.builder().id(USER_ID).balance(200).build();
        var dto = new PaymentStatusDto(UUID.randomUUID(), PaymentStatus.SUCCEEDED, "conf-url");
        var payment = Payment.builder()
                .id(dto.id())
                .status(dto.event())
                .amount(100)
                .type(PaymentType.TOP_UP)
                .user(u)
                .confirmationUrl(dto.url()).build();
        var amount = u.getBalance() + payment.getAmount();
        when(paymentRepository.findById(dto.id())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        paymentService.handleNotification(dto);
        verify(paymentRepository).save(argThat(p -> p.getId().equals(dto.id())
                && p.getStatus().equals(dto.event())
                && p.getUser().getBalance().equals(amount)));

    }

    @Test
    void handleNotification_shouldNotTopUp_whenPurchaseCancelled() {
        var u = User.builder().id(USER_ID).balance(200).build();
        var dto = new PaymentStatusDto(UUID.randomUUID(), PaymentStatus.CANCELED, "conf-url");
        var payment = Payment.builder()
                .id(dto.id())
                .status(dto.event())
                .amount(100)
                .type(PaymentType.TOP_UP)
                .user(u)
                .confirmationUrl(dto.url()).build();
        when(paymentRepository.findById(dto.id())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        paymentService.handleNotification(dto);
        verify(paymentRepository).save(argThat(p -> p.getId().equals(dto.id())
                && p.getStatus().equals(dto.event())
                && p.getUser().getBalance().equals(u.getBalance())));

    }

    @Test
    void handleNotification_shouldRefund_whenAccountsLack() throws UnspecifiedShopInformation, BadRequestException, IOException {
        var u = User.builder().id(USER_ID).build();
        var dto = new PaymentStatusDto(UUID.randomUUID(), PaymentStatus.SUCCEEDED, "conf-url");
        var payment = Payment.builder()
                .id(dto.id())
                .status(dto.event())
                .source(PaymentSource.CART)
                .type(PaymentType.PURCHASE)
                .amount(200)
                .user(u)
                .confirmationUrl(dto.url()).build();
        when(paymentRepository.findById(dto.id())).thenReturn(Optional.of(payment));
        when(accountService.sellAccounts(any())).thenThrow(AccountLackException.class);

        var result = paymentService.handleNotification(dto);
        assertNull(result);
        verify(yookassa, times(1)).createRefund(any(RefundRequest.class));
        verify(paymentRepository, never()).save(any());
        verify(notificationService, never()).sendPayment(payment);
        verify(mailService, never()).sendAccounts(eq(payment), any());
        verify(userProductRepository, never()).deleteAllByUser(payment.getUser());
    }
}
