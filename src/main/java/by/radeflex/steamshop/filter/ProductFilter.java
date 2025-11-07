package by.radeflex.steamshop.filter;

import lombok.Builder;

@Builder
public record ProductFilter(
        String title,
        Integer priceMin,
        Integer priceMax
) {
}
