package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Данные продукта")
public record ProductReadDto(
        @Schema(description = "ID продукта", example = "1")
        Integer id,
        @Schema(description = "Название продукта", example = "Counter-Strike 2")
        String title,
        @Schema(description = "Описание продукта", example = "Популярный тактический шутер от Valve")
        String description,
        @Schema(description = "Цена в рублях", example = "499")
        Integer price,
        @Schema(description = "URL превью-изображения", example = "550e8400-e29b-41d4-a716-446655440000")
        String previewUrl
) {
}
