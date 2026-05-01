package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.UserProduct;
import jakarta.persistence.Tuple;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.title", target = "title")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "product.previewUrl", target = "previewUrl")
    CartProductReadDto mapFrom(UserProduct up);

    default CartProductReadDto mapFrom(Tuple t) {
            var userProduct = t.get("userProduct", UserProduct.class);
            var isEnough = t.get("isEnough", Boolean.class);
            Product product = userProduct.getProduct();
            return CartProductReadDto.builder()
                    .id(userProduct.getId())
                    .productId(product.getId())
                    .title(product.getTitle())
                    .price(product.getPrice())
                    .previewUrl(product.getPreviewUrl())
                    .quantity(userProduct.getQuantity())
                    .isEnough(isEnough)
                    .build();
    }
}
