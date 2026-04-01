package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.mapper.AccountMapper;
import by.radeflex.steamshop.repository.AccountRepository;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    private final int USER_ID = 2;
    private final int PRODUCT_ID = 1;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PaymentItemRepository paymentItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Spy
    private AccountMapper accountMapper;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentUserEntity())
                .thenReturn(User.builder().id(USER_ID).build());
    }

    @Test
    void create_shouldReturnDto_ifProductExists() {
        var dto = new AccountCreateDto("example", "passwd", "example@gmail.com", "passwd", PRODUCT_ID);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(Product.builder().id(PRODUCT_ID).build()));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = accountService.create(dto);
        assertTrue(result.isPresent());
        verify(productRepository).findById(PRODUCT_ID);
        verify(accountRepository).save(argThat(a ->
                a.getProduct().getId().equals(PRODUCT_ID)
                && a.getUsername().equals(dto.username())
                && a.getPassword().equals(dto.password())
                && a.getEmail().equals(dto.email())
                && a.getEmailPassword().equals(dto.emailPassword())
                && a.getCreatedBy().equals(currentUserService.getCurrentUserEntity())));
    }

    @Test
    void create_shouldReturnEmpty_ifProductNotExists() {
        var dto = new AccountCreateDto("example", "passwd", "example@gmail.com", "passwd", PRODUCT_ID);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        var result = accountService.create(dto);
        assertTrue(result.isEmpty());
        verify(productRepository).findById(PRODUCT_ID);
        verify(accountRepository, never()).save(any());
    }

    enum ReservationMode {
        RESERVE, UNRESERVE
    }

    @ParameterizedTest
    @EnumSource(ReservationMode.class)
    void reserveAndUnreserve_shouldWork(ReservationMode mode) {
        Payment pm = Payment.builder().id(UUID.randomUUID()).build();
        List<PaymentItem> items = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        var statusFrom = mode.equals(ReservationMode.RESERVE) ? AccountStatus.AVAILABLE : AccountStatus.RESERVED;
        var statusTo = mode.equals(ReservationMode.UNRESERVE) ? AccountStatus.AVAILABLE : AccountStatus.RESERVED;
        int sum = 0;
        for (int i = 1; i < 5; i++) {
            var p = Product.builder()
                    .id(i + 2)
                    .title("example " + (i + 2))
                    .price(i * 50)
                    .build();
            items.add(PaymentItem.builder()
                    .product(p)
                    .payment(pm)
                    .quantity(1).build());
            accounts.add(Account.builder().product(p).status(statusFrom).build());
            sum += p.getPrice();
        }
        pm.setAmount(sum);
        when(accountRepository.findByProductIdAndStatus(any(), eq(statusFrom), any()))
                .thenAnswer(inv -> List.of(Account.builder()
                        .product(items.get((int)inv.getArgument(0) - 3).getProduct())
                        .status(inv.getArgument(1)).build()));
        when(paymentItemRepository.findAllByPayment(pm)).thenReturn(items);

        if (mode.equals(ReservationMode.RESERVE))
            accountService.reserve(pm);
        else accountService.unreserve(pm);
        verify(accountRepository, times(items.size()))
                .findByProductIdAndStatus(anyInt(), any(AccountStatus.class), any());
        verify(accountRepository, times(accounts.size())).save(argThat(a ->
                a.getProduct() != null
                && a.getStatus().equals(statusTo)));
    }

    @Test
    void reserve_shouldThrow_whenAccountsLack() {
        Payment pm = Payment.builder().id(UUID.randomUUID()).build();
        List<PaymentItem> items = new ArrayList<>();
        int sum = 0;
        for (int i = 1; i < 5; i++) {
            var p = Product.builder()
                    .id(i + 2)
                    .title("example " + (i + 2))
                    .price(i * 50)
                    .build();
            items.add(PaymentItem.builder()
                    .product(p)
                    .payment(pm)
                    .quantity(1).build());
            sum += p.getPrice();
        }
        pm.setAmount(sum);
        when(accountRepository.findByProductIdAndStatus(any(), eq(AccountStatus.AVAILABLE), any()))
                .thenAnswer(inv -> {
                    int ind = (int)inv.getArgument(0) - 3;
                    if (ind == 2) return Collections.emptyList();
                    return List.of(Account.builder()
                        .product(items.get(ind).getProduct())
                        .status(inv.getArgument(1)).build());
                });
        when(paymentItemRepository.findAllByPayment(pm)).thenReturn(items);

        assertThrows(AccountLackException.class, () -> accountService.reserve(pm));
        verify(accountRepository, times(items.size()))
                .findByProductIdAndStatus(anyInt(), any(AccountStatus.class), any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void sellAccounts_shouldWork() {
        Payment pm = Payment.builder().id(UUID.randomUUID()).build();
        List<PaymentItem> items = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        int sum = 0;
        for (int i = 1; i < 5; i++) {
            var p = Product.builder()
                    .id(i + 2)
                    .title("example " + (i + 2))
                    .price(i * 50)
                    .build();
            items.add(PaymentItem.builder()
                    .product(p)
                    .payment(pm)
                    .quantity(1).build());
            accounts.add(Account.builder().product(p).status(AccountStatus.RESERVED).build());
            sum += p.getPrice();
        }
        pm.setAmount(sum);
        when(accountRepository.findByProductIdAndStatus(any(), eq(AccountStatus.RESERVED), any()))
                .thenAnswer(inv -> List.of(Account.builder()
                            .product(items.get((int)inv.getArgument(0) - 3).getProduct())
                            .status(inv.getArgument(1)).build()));
        when(paymentItemRepository.findAllByPayment(pm)).thenReturn(items);
        accountService.sellAccounts(pm);
        verify(accountRepository, times(items.size()))
                .findByProductIdAndStatus(anyInt(), any(AccountStatus.class), any());
        verify(accountRepository, times(accounts.size())).save(argThat(a ->
                a.getProduct() != null
                        && a.getStatus().equals(AccountStatus.SOLD)));
    }

    @Test
    void sellAccounts_shouldThrow_whenAccountsLack() {
        Payment pm = Payment.builder().id(UUID.randomUUID()).build();
        List<PaymentItem> items = new ArrayList<>();
        int sum = 0;
        for (int i = 1; i < 5; i++) {
            var p = Product.builder()
                    .id(i + 2)
                    .title("example " + (i + 2))
                    .price(i * 50)
                    .build();
            items.add(PaymentItem.builder()
                    .product(p)
                    .payment(pm)
                    .quantity(1).build());
            sum += p.getPrice();
        }
        pm.setAmount(sum);
        when(accountRepository.findByProductIdAndStatus(any(), eq(AccountStatus.RESERVED), any()))
                .thenAnswer(inv -> {
                    int ind = (int)inv.getArgument(0) - 3;
                    if (ind == 2) return Collections.emptyList();
                    return List.of(Account.builder()
                            .product(items.get(ind).getProduct())
                            .status(inv.getArgument(1)).build());
                });
        when(paymentItemRepository.findAllByPayment(pm)).thenReturn(items);

        assertThrows(AccountLackException.class, () -> accountService.sellAccounts(pm));
        verify(accountRepository, times(items.size()))
                .findByProductIdAndStatus(anyInt(), any(AccountStatus.class), any());
        verify(accountRepository, never()).save(any());
    }
}
