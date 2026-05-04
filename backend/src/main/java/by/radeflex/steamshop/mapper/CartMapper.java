package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CartProductReadDto;
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

    default CartProductReadDto map(UserProduct userProduct, Boolean isEnough) {
        CartProductReadDto dto = mapFrom(userProduct);
        return dto.withEnough(isEnough);
    }

    default CartProductReadDto mapFrom(Tuple t) {
            var userProduct = t.get("userProduct", UserProduct.class);
            var isEnough = t.get("isEnough", Boolean.class);
            return map(userProduct, isEnough);
    }
}
