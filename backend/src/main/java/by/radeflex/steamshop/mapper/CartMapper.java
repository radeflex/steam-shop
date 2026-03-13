package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.UserProduct;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {
    public CartProductReadDto mapFrom(UserProduct userProduct) {
        Product product = userProduct.getProduct();
        return CartProductReadDto.builder()
                .id(userProduct.getId())
                .productId(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .previewUrl(product.getPreviewUrl())
                .quantity(userProduct.getQuantity())
                .build();
    }

    public CartProductReadDto mapFrom(Tuple t) {
            var userProduct = t.get("userProduct", UserProduct.class);
            var isEnough = t.get("isEnough", Boolean.class);
            Product product = userProduct.getProduct();
            return CartProductReadDto.builder()
                    .id(userProduct.getId())
                    .productId(product.getId())
                    .title(product.getTitle())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .previewUrl(product.getPreviewUrl())
                    .quantity(userProduct.getQuantity())
                    .isEnough(isEnough)
                    .build();
    }
}
