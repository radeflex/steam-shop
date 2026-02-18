package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.UserProduct;
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
}
