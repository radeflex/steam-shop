package by.radeflex.steamshop.dto;

import java.util.Map;

public record PurchaseCreateDto(
        Map<Integer, Integer> products
) {
}
