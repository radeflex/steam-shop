package by.radeflex.steamshop.service;

import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserProduct;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.mapper.CartMapper;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductRepository;
import by.radeflex.steamshop.service.CartService;
import by.radeflex.steamshop.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    private final int USER_PRODUCT_ID = 2;
    private final int USER_ID = 1;
    private final int PRODUCT_ID = 4;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserProductRepository userProductRepository;
    @Spy
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentUserEntity())
                .thenReturn(User.builder().id(USER_ID).build());
    }

    @Test
    void create_shouldReturnDto_whenProductAvailable() {
        User user = currentUserService.getCurrentUserEntity();
        Product product = Product.builder()
                .id(PRODUCT_ID)
                .price(123)
                .title("example")
                .build();
        UserProduct userProduct = UserProduct.builder()
                        .user(user)
                        .product(product)
                        .quantity(1).build();

        when(productRepository.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product));
        when(userProductRepository.hasEnoughAccounts(eq(product), anyInt()))
                .thenReturn(true);

        var result = cartService.create(PRODUCT_ID);

        assertTrue(result.isPresent());
        verify(cartMapper).mapFrom(userProduct);
        verify(userProductRepository).hasEnoughAccounts(any(), anyInt());
        verify(userProductRepository).save(argThat(up ->
                up.getUser().getId().equals(USER_ID) &&
                        up.getProduct().getId().equals(PRODUCT_ID) &&
                        up.getQuantity() == 1));
    }
    @Test
    void create_shouldThrow_whenUserProductExists() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product()));
        when(userProductRepository.existsByUserAndProduct(any(), any())).thenReturn(true);

        assertThrows(ObjectExistsException.class, () -> cartService.create(PRODUCT_ID));
        verify(userProductRepository).existsByUserAndProduct(any(), any());
        verify(userProductRepository, never()).save(any());
    }
    @Test
    void create_shouldReturnEmpty_whenProductNotExists() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        var result = cartService.create(PRODUCT_ID);
        assertTrue(result.isEmpty());
        verify(productRepository).findById(PRODUCT_ID);
        verify(userProductRepository, never()).save(any());
    }

    @Test
    void updateQuantity_shouldReturnDto_whenProductAvailableAndUserProductExists() {
        User user = currentUserService.getCurrentUserEntity();
        Product product = Product.builder().id(PRODUCT_ID).build();

        int quantity = 3;
        UserProduct userProduct = UserProduct.builder()
                .id(USER_PRODUCT_ID)
                .user(user)
                .product(product)
                .quantity(1)
                .build();
        when(userProductRepository.findById(userProduct.getId()))
                .thenReturn(Optional.of(userProduct));
        when(userProductRepository.hasEnoughAccounts(eq(userProduct.getProduct()), eq(quantity)))
                .thenReturn(true);

        var result = cartService.updateQuantity(userProduct.getId(), quantity);
        assertTrue(result.isPresent());
        verify(userProductRepository)
                .hasEnoughAccounts(eq(userProduct.getProduct()), eq(quantity));
        verify(userProductRepository).saveAndFlush(argThat(up ->
                up.getId().equals(USER_PRODUCT_ID) &&
                up.getQuantity().equals(quantity)));
        userProduct.setQuantity(quantity);
        verify(cartMapper).mapFrom(userProduct);
    }
    @Test
    void updateQuantity_shouldThrow_whenProductUnavailable() {
        when(userProductRepository.findById(USER_PRODUCT_ID))
                .thenReturn(Optional.of(new UserProduct()));
        when(userProductRepository.hasEnoughAccounts(any(), anyInt()))
                .thenReturn(false);

        assertThrows(AccountLackException.class,
                () -> cartService.updateQuantity(USER_PRODUCT_ID, 3));
    }

    @Test
    void updateQuantity_shouldReturnEmpty_whenUserProductNotExists() {
        when(userProductRepository.findById(USER_PRODUCT_ID))
                .thenReturn(Optional.empty());

        var result = cartService.updateQuantity(USER_PRODUCT_ID, 1);
        assertTrue(result.isEmpty());
        verify(userProductRepository).findById(USER_PRODUCT_ID);
        verify(userProductRepository, never()).save(any());
    }

    @Test
    void delete_shouldReturnTrue_whenUserProductExists() {
        var userProduct = UserProduct.builder().id(USER_PRODUCT_ID).build();
        when(userProductRepository.findById(USER_PRODUCT_ID))
                .thenReturn(Optional.of(userProduct));
        var result = cartService.delete(USER_PRODUCT_ID);
        assertTrue(result);
        verify(userProductRepository).delete(argThat(up ->
                up.getId().equals(USER_PRODUCT_ID)));
    }

    @Test
    void delete_shouldReturnFalse_whenUserProductNotExists() {
        when(userProductRepository.findById(USER_PRODUCT_ID))
                .thenReturn(Optional.empty());
        var result = cartService.delete(USER_PRODUCT_ID);
        assertFalse(result);
        verify(userProductRepository, never()).delete(any());
    }
}
