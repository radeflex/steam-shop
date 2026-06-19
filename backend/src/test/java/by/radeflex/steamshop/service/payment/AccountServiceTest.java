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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Mock
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
        var dto = new AccountCreateDto(
                "example",
                "passwd",
                "example@gmail.com",
                "passwd",
                PRODUCT_ID
        );

        var user = User.builder().id(USER_ID).build();

        var account = Account.builder()
                .username(dto.username())
                .password(dto.password())
                .email(dto.email())
                .emailPassword(dto.emailPassword())
                .product(Product.builder().id(PRODUCT_ID).build())
                .createdBy(user)
                .build();

        var readDto = mock(by.radeflex.steamshop.dto.AccountReadDto.class);

        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(Product.builder().id(PRODUCT_ID).build()));

        when(accountMapper.mapFrom(dto, user))
                .thenReturn(account);

        when(accountRepository.save(account))
                .thenReturn(account);

        when(accountMapper.mapFrom(account))
                .thenReturn(readDto);

        var result = accountService.create(dto);

        assertTrue(result.isPresent());

        verify(productRepository).findById(PRODUCT_ID);
        verify(accountMapper).mapFrom(dto, user);
        verify(accountRepository).save(account);
        verify(accountMapper).mapFrom(account);
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
        Payment pm = Payment.builder()
                .id(UUID.randomUUID())
                .build();

        var statusFrom =
                mode == ReservationMode.RESERVE
                        ? AccountStatus.AVAILABLE
                        : AccountStatus.RESERVED;

        var statusTo =
                mode == ReservationMode.RESERVE
                        ? AccountStatus.RESERVED
                        : AccountStatus.AVAILABLE;

        List<PaymentItem> items = List.of(
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build()
        );

        List<Account> accounts = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            accounts.add(Account.builder()
                    .status(statusFrom)
                    .product(Product.builder().id(i + 1).build())
                    .build());
        }

        when(paymentItemRepository.findAllByPayment(pm))
                .thenReturn(items);

        when(accountRepository.findByStatus(
                pm.getId(),
                statusFrom.name()))
                .thenReturn(accounts);

        if (mode == ReservationMode.RESERVE) {
            accountService.reserve(pm);
        } else {
            accountService.unreserve(pm);
        }

        verify(accountRepository)
                .findByStatus(pm.getId(), statusFrom.name());

        verify(accountRepository)
                .saveAll(argThat(accs -> {
                    List<Account> list = new ArrayList<>();
                    accs.forEach(list::add);

                    return list.size() == 4
                            && list.stream()
                            .allMatch(a -> a.getStatus() == statusTo);
                }));
    }

    @Test
    void reserve_shouldThrow_whenAccountsLack() {
        Payment pm = Payment.builder()
                .id(UUID.randomUUID())
                .build();

        List<PaymentItem> items = List.of(
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build()
        );

        List<Account> accounts = List.of(
                Account.builder().status(AccountStatus.AVAILABLE).build(),
                Account.builder().status(AccountStatus.AVAILABLE).build(),
                Account.builder().status(AccountStatus.AVAILABLE).build()
        );

        when(paymentItemRepository.findAllByPayment(pm))
                .thenReturn(items);

        when(accountRepository.findByStatus(
                pm.getId(),
                AccountStatus.AVAILABLE.name()))
                .thenReturn(accounts);

        assertThrows(
                AccountLackException.class,
                () -> accountService.reserve(pm)
        );

        verify(accountRepository, never()).saveAll(any());
    }

    @Test
    void sellAccounts_shouldWork() {
        Payment pm = Payment.builder()
                .id(UUID.randomUUID())
                .build();

        Product product = Product.builder()
                .title("Steam")
                .build();

        List<PaymentItem> items = List.of(
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build()
        );

        List<Account> accounts = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            accounts.add(Account.builder()
                    .status(AccountStatus.RESERVED)
                    .product(product)
                    .build());
        }

        when(paymentItemRepository.findAllByPayment(pm))
                .thenReturn(items);

        when(accountRepository.findByStatus(
                pm.getId(),
                AccountStatus.RESERVED.name()))
                .thenReturn(accounts);

        accountService.sellAccounts(pm);

        verify(accountRepository)
                .saveAll(argThat(accs -> {
                    List<Account> list = new ArrayList<>();
                    accs.forEach(list::add);

                    return list.size() == 4
                            && list.stream()
                            .allMatch(a -> a.getStatus() == AccountStatus.SOLD);
                }));
    }

    @Test
    void sellAccounts_shouldThrow_whenAccountsLack() {
        Payment pm = Payment.builder()
                .id(UUID.randomUUID())
                .build();

        List<PaymentItem> items = List.of(
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build(),
                PaymentItem.builder().quantity(1).build()
        );

        List<Account> accounts = List.of(
                Account.builder().status(AccountStatus.RESERVED).build(),
                Account.builder().status(AccountStatus.RESERVED).build(),
                Account.builder().status(AccountStatus.RESERVED).build()
        );

        when(paymentItemRepository.findAllByPayment(pm))
                .thenReturn(items);

        when(accountRepository.findByStatus(
                pm.getId(),
                AccountStatus.RESERVED.name()))
                .thenReturn(accounts);

        assertThrows(
                AccountLackException.class,
                () -> accountService.sellAccounts(pm)
        );

        verify(accountRepository, never()).saveAll(any());
    }
}
