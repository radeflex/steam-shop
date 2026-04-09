package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.entity.*;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Component;

@Component
public class ProductHistoryMapper {
    public UserProductHistory mapFrom(PaymentItem pi) {
        return UserProductHistory.builder()
                .product(pi.getProduct())
                .user(pi.getPayment().getUser())
                .title(pi.getProduct().getTitle())
                .price(pi.getProduct().getPrice())
                .payment(pi.getPayment())
                .quantity(pi.getQuantity())
                .build();
    }
    public ProductHistoryReadDto mapFrom(Tuple tuple) {
        return ProductHistoryReadDto.builder()
                .productId(tuple.get("productId", Integer.class))
                .userId(tuple.get("userId", Integer.class))
                .title(tuple.get("title", String.class))
                .price(tuple.get("price", Integer.class))
                .quantity(tuple.get("quantity", Long.class).intValue())
                .build();
    }
}
