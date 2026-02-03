package by.radeflex.steamshop.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record PurchaseCreateDto(
        Map<Integer, Integer> products
) {
}
