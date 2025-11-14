package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.UserProductHistory;
import org.springframework.stereotype.Component;

import static by.radeflex.steamshop.service.AuthService.getCurrentUser;

@Component
public class ProductHistoryMapper {
    public UserProductHistory mapFrom(Product product, int quantity) {
        return UserProductHistory.builder()
                .product(product)
                .user(getCurrentUser())
                .title(product.getTitle())
                .price(product.getPrice())
                .quantity(quantity)
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
                .build();
    }
}
