package by.radeflex.steamshop.dto;

import lombok.Builder;

@Builder
public record CartProductReadDto(
        Integer id,
        Integer productId,
        String title,
        String description,
        Integer price,
        String previewUrl,
        Integer quantity
) {
}
