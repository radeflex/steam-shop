package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Товар в корзине")
public record CartProductReadDto(
        @Schema(description = "ID позиции в корзине", example = "1")
        Integer id,
        @Schema(description = "ID продукта", example = "5")
        Integer productId,
        @Schema(description = "Название продукта", example = "Counter-Strike 2")
        String title,
        @Schema(description = "Описание продукта")
        String description,
        @Schema(description = "Цена за единицу (в рублях)", example = "499")
        Integer price,
        @Schema(description = "URL превью-изображения", example = "550e8400-e29b-41d4-a716-446655440000")
        String previewUrl,
        @Schema(description = "Количество в корзине", example = "2")
        Integer quantity,
        @Schema(description = "Достаточно ли аккаунтов для покупки выбранного количества", example = "true")
        boolean isEnough
) {
}
