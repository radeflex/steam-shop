package by.radeflex.steamshop.service.payment;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.entity.*;
import by.radeflex.steamshop.mapper.AccountMapper;
import by.radeflex.steamshop.repository.AccountRepository;
import by.radeflex.steamshop.repository.PaymentItemRepository;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
}
