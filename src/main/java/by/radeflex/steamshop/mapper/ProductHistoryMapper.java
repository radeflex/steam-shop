package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.entity.UserProductHistory;
import org.springframework.stereotype.Component;

@Component
public class ProductHistoryMapper {
    public ProductHistoryReadDto mapFrom(UserProductHistory userProductHistory) {
        return ProductHistoryReadDto.builder()
                .id(userProductHistory.getId())
                .productId(userProductHistory.getProduct().getId())
                .userId(userProductHistory.getUser().getId())
                .title(userProductHistory.getProduct().getTitle())
                .price(userProductHistory.getProduct().getPrice())
                .quantity(userProductHistory.getQuantity())
                .build();
    }
}
