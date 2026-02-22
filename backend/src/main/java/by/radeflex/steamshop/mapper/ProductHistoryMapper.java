package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ProductHistoryMapper {
    public UserProductHistory mapFrom(PaymentItem pi) {
        return UserProductHistory.builder()
                .product(pi.getProduct())
                .user(pi.getPayment().getUser())
                .title(pi.getProduct().getTitle())
                .price(pi.getProduct().getPrice())
                .quantity(pi.getQuantity())
                .build();
    }
    public ProductHistoryReadDto mapFrom(UserProductHistory userProductHistory) {
        return ProductHistoryReadDto.builder()
                .id(userProductHistory.getId())
                .productId(userProductHistory.getProduct().getId())
                .userId(userProductHistory.getUser().getId())
                .title(userProductHistory.getProduct().getTitle())
                .price(userProductHistory.getProduct().getPrice())
                .quantity(userProductHistory.getQuantity())
                .createdAt(userProductHistory.getCreatedAt())
                .build();
    }
}
