package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.ProductCreateDto;
import by.radeflex.steamshop.dto.ProductUpdateDto;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.mapper.ProductMapper;
import by.radeflex.steamshop.repository.ProductRepository;
import com.querydsl.core.types.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    private final int PRODUCT_ID = 1;
    private final MockMultipartFile PREVIEW = new MockMultipartFile("avatar.png", new byte[0]);
    @Mock
    private ProductRepository productRepository;
    @Spy
    private ProductMapper productMapper;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_shouldWork() {
        var dto = new ProductCreateDto("example", "example", 100);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(imageService.upload(eq(PREVIEW))).thenReturn(UUID.randomUUID().toString());

        productService.create(dto, PREVIEW);
        verify(productRepository).save(any(Product.class));
        verify(imageService).upload(any(MultipartFile.class));
        verify(productMapper).mapFrom(any(Product.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "description"})
    void create_shouldThrow_whenFieldsExist(String value) {
        var dto = new ProductCreateDto("example", "example", 100);

        when(productRepository.exists(any(Predicate.class))).thenReturn(true);

        var e = assertThrows(ObjectExistsException.class,
                () -> productService.create(dto, PREVIEW));
        assertTrue(e.getErrors().containsKey(value));
        verify(productRepository, never()).save(any());
        verify(imageService, never()).upload(any());
        verify(imageService, never()).delete(any());
    }

    enum Field {
        TITLE, DESCRIPTION, PRICE
    }

    @ParameterizedTest
    @EnumSource(Field.class)
    void update_shouldReturnDto(Field nullValue) {
        ProductUpdateDto dto = switch (nullValue) {
            case TITLE -> new ProductUpdateDto(null, "example", 100);
            case DESCRIPTION -> new ProductUpdateDto("example", null, 100);
            case PRICE -> new ProductUpdateDto("example", "example", null);

        };
        Product old = Product.builder()
                        .id(PRODUCT_ID)
                        .title("different")
                        .description("different")
                        .previewUrl("preview")
                        .price(123).build();

        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(old));

        var result = productService.update(PRODUCT_ID, dto, null);
        assertTrue(result.isPresent());
        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository).saveAndFlush(argThat(p ->
                p.getPrice().equals(dto.price() == null ? old.getPrice() : dto.price())
                && p.getDescription().equals(dto.description() == null ? old.getDescription() : dto.description())
                && p.getTitle().equals(dto.title() == null ? old.getTitle() : dto.title())
                && p.getPreviewUrl().equals(old.getPreviewUrl())));
        verify(imageService, never()).delete(any());
        verify(imageService, never()).upload(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "description"})
    void update_shouldThrow_whenFieldsExist(String value) {
        var dto = new ProductUpdateDto("example", "example", 100);

        when(productRepository.exists(any(Predicate.class))).thenReturn(true);

        var e = assertThrows(ObjectExistsException.class,
                () -> productService.update(PRODUCT_ID, dto, PREVIEW));
        assertTrue(e.getErrors().containsKey(value));
        verify(productRepository, never()).saveAndFlush(any());
        verify(imageService, never()).upload(any());
        verify(imageService, never()).delete(any());
    }

    @Test
    void update_shouldUpdateAvatar() {
        var dto = new ProductUpdateDto("example", "example", 100);
        var oldPreviewUrl = "preview";
        Product old = Product.builder()
                .id(PRODUCT_ID)
                .previewUrl(oldPreviewUrl).build();
        var previewUrl = UUID.randomUUID().toString();

        when(productRepository.saveAndFlush(any(Product.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(old));
        when(imageService.upload(eq(PREVIEW))).thenReturn(previewUrl);

        var result = productService.update(PRODUCT_ID, dto, PREVIEW);
        assertTrue(result.isPresent());
        verify(productRepository).saveAndFlush(argThat(p -> p.getPreviewUrl().equals(previewUrl)));
        verify(imageService).delete(eq(oldPreviewUrl));
        verify(imageService).upload(PREVIEW);
    }

    @Test
    void update_shouldReturnEmpty_whenProductNotExists() {
        var dto = new ProductUpdateDto("example", "example", 100);

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        var result = productService.update(PRODUCT_ID, dto, null);
        assertTrue(result.isEmpty());
        verify(productRepository, never()).saveAndFlush(any());
        verify(imageService, never()).delete(any());
        verify(imageService, never()).upload(any());
    }

    @Test
    void delete_shouldReturnTrue_whenProductExists() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(new Product()));

        var result = productService.delete(PRODUCT_ID);
        assertTrue(result);
        verify(productRepository).delete(any());
    }

    @Test
    void delete_shouldReturnFalse_whenProductNotExists() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        var result = productService.delete(PRODUCT_ID);
        assertFalse(result);
        verify(productRepository, never()).delete(any());
    }
}
