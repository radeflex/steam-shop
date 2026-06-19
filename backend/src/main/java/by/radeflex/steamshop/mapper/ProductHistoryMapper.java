package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.entity.*;
import jakarta.persistence.Tuple;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductHistoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "payment.user")
    @Mapping(target = "title", source = "product.title")
    @Mapping(target = "price", source = "product.price")
    UserProductHistory mapFrom(PaymentItem pi);

    default ProductHistoryReadDto mapFrom(Tuple tuple) {
        return ProductHistoryReadDto.builder()
                .productId(tuple.get("productId", Integer.class))
                .userId(tuple.get("userId", Integer.class))
                .title(tuple.get("title", String.class))
                .price(tuple.get("price", Integer.class))
                .quantity(tuple.get("quantity", Long.class).intValue())
                .build();
    }
}
