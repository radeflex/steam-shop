package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductRepository;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.MailService;
import by.radeflex.steamshop.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    private final int USER_ID = 2;
    private final int PRODUCT_ID = 1;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MailService mailService;
    @Mock
    private AccountService accountService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private UserProductRepository userProductRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void purchaseViaBalance_shouldReturnTrue() {
        var bal = 300;
        var u = User.builder().id(USER_ID).balance(bal).build();
        var p = Product.builder().id(PRODUCT_ID).price(bal).build();
        var pm = Payment.builder()
                .id(UUID.randomUUID())
                .amount(bal).build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(paymentService.createPaymentViaBalance(u, p)).thenReturn(pm);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(p));

        var result = orderService.purchaseViaBalance(PRODUCT_ID);
        verify(accountService, times(1)).sellAccounts(eq(pm));
        verify(mailService, times(1)).sendAccounts(eq(pm), any());
        verify(notificationService, times(1)).sendPayment(eq(pm));
        assertEquals(0, u.getBalance());
        assertTrue(result);
    }

    @Test
    void purchaseViaBalance_shouldReturnFalse_whenBalanceLack() {
        var bal = 300;
        var u = User.builder().id(USER_ID).balance(bal).build();
        var p = Product.builder().id(PRODUCT_ID).price(bal + 1).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(p));

        var result = orderService.purchaseViaBalance(PRODUCT_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verifyNoMoreInteractions(paymentService, accountService,
                notificationService, mailService);
        assertEquals(bal, u.getBalance());
        assertFalse(result);
    }

    @Test
    void purchaseViaBalance_shouldReturnFalse_whenProductNotExists() {
        var u = User.builder().id(USER_ID).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        var result = orderService.purchaseViaBalance(PRODUCT_ID);
        verify(productRepository).findById(PRODUCT_ID);
        verifyNoMoreInteractions(paymentService, accountService,
                notificationService, mailService);
        assertFalse(result);
    }

    @Test
    void purchaseCartViaBalance_shouldReturnTrue() {
        var u = User.builder().id(USER_ID).build();
        var pm = Payment.builder().id(UUID.randomUUID()).build();
        List<UserProduct> cart = new ArrayList<>();
        int sum = 0;
        for (int i = 1; i < 4; i++) {
            var p = Product.builder()
                    .id(i + 1)
                    .price(i * 10)
                    .build();
            cart.add(UserProduct.builder()
                    .user(u)
                    .product(p)
                    .quantity(1).build());
            sum += p.getPrice();
        }
        u.setBalance(sum);
        
        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(userProductRepository.findAvailableByUser(u)).thenReturn(cart);
        when(paymentService.createPaymentCartViaBalance(u, sum, cart))
                .thenReturn(pm);
        
        var result = orderService.purchaseCartViaBalance();
        assertTrue(result);
        assertEquals(0, u.getBalance());
        verify(userProductRepository).findAvailableByUser(u);
        verify(paymentService, times(1)).createPaymentCartViaBalance(u, sum, cart);
        verify(userRepository).findById(USER_ID);
        verify(accountService, times(1)).sellAccounts(pm);
        verify(notificationService, times(1)).sendPayment(pm);
        verify(mailService).sendAccounts(eq(pm), any());
        verify(userProductRepository).deleteAllByUser(u);
    }

    @Test
    void purchaseCartViaBalance_shouldReturnFalse_whenBalanceLack() {
        var oldBalance = 0;
        var u = User.builder().id(USER_ID).balance(oldBalance).build();
        List<UserProduct> cart = new ArrayList<>();

        for (int i = 1; i < 4; i++) {
            var p = Product.builder()
                    .id(i + 1)
                    .price(i * 10)
                    .build();
            cart.add(UserProduct.builder()
                    .user(u)
                    .product(p)
                    .quantity(1).build());
        }

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(userProductRepository.findAvailableByUser(u)).thenReturn(cart);

        var result = orderService.purchaseCartViaBalance();
        assertFalse(result);
        assertEquals(oldBalance, u.getBalance());
        verify(userRepository).findById(USER_ID);
        verify(userProductRepository).findAvailableByUser(u);
        verifyNoMoreInteractions(paymentService, accountService,
                notificationService, mailService, userProductRepository);
    }

    @Test
    void purchaseCartViaBalance_shouldThrow_whenCartIsEmpty() {
        var oldBalance = 0;
        var u = User.builder().id(USER_ID).balance(oldBalance).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(userProductRepository.findAvailableByUser(u)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> orderService.purchaseCartViaBalance());
        assertEquals(oldBalance, u.getBalance());
        verify(userRepository).findById(USER_ID);
        verify(userProductRepository).findAvailableByUser(u);
        verifyNoMoreInteractions(paymentService, accountService,
                notificationService, mailService, userProductRepository);
    }

    @Test
    void purchaseViaCard_shouldReturnUrl() {
        var u = User.builder().id(USER_ID).build();
        var p = Product.builder().id(PRODUCT_ID).price(100).build();
        var up = List.of(UserProduct.builder().user(u).product(p).quantity(1).build());
        var pm = Payment.builder().id(UUID.randomUUID())
                .confirmationUrl("confirmation.url").build();

        when(currentUserService.getCurrentUserEntity()).thenReturn(u);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(p));
        when(paymentService.createPaymentViaCard(p.getPrice(), u, PaymentSource.CLICK, up))
                .thenReturn(pm);

        var result = orderService.purchaseViaCard(PRODUCT_ID);
        assertTrue(result.isPresent());
        verify(productRepository).findById(PRODUCT_ID);
        verify(paymentService).createPaymentViaCard(p.getPrice(), u, PaymentSource.CLICK, up);
        verify(notificationService).sendPayment(pm);
    }

    @Test
    void purchaseViaCard_shouldReturnEmpty_whenProductNotExists() {
        var u = User.builder().id(USER_ID).build();

        when(currentUserService.getCurrentUserEntity()).thenReturn(u);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        var result = orderService.purchaseViaCard(PRODUCT_ID);
        assertTrue(result.isEmpty());
        verify(productRepository).findById(PRODUCT_ID);
        verifyNoMoreInteractions(paymentService, notificationService);
    }

    @Test
    void purchaseCartViaCard_shouldReturnUrl() {
        var u = User.builder().id(USER_ID).build();
        var pm = Payment.builder().confirmationUrl("confirmation.url").build();
        List<UserProduct> cart = new ArrayList<>();
        int sum = 0;

        for (int i = 1; i < 4; i++) {
            var p = Product.builder()
                    .id(i + 1)
                    .price(i * 10)
                    .build();
            cart.add(UserProduct.builder()
                    .user(u)
                    .product(p)
                    .quantity(1).build());
            sum += p.getPrice();
        }

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(userProductRepository.findAvailableByUser(u)).thenReturn(cart);
        when(paymentService.createPaymentViaCard(sum, u, PaymentSource.CART, cart))
                .thenReturn(pm);

        var result = orderService.purchaseCartViaCard();
        assertNotNull(result);
        verify(userProductRepository).findAvailableByUser(u);
        verify(paymentService, times(1)).createPaymentViaCard(sum, u, PaymentSource.CART, cart);
        verify(notificationService, times(1)).sendPayment(pm);
    }

    @Test
    void purchaseCartViaCard_shouldThrow_whenCartIsEmpty() {
        var u = User.builder().id(USER_ID).build();

        when(currentUserService.getCurrentUserId()).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(u));
        when(userProductRepository.findAvailableByUser(u)).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> orderService.purchaseCartViaCard());
        verify(userProductRepository).findAvailableByUser(u);
        verifyNoMoreInteractions(paymentService, notificationService);
    }
}
