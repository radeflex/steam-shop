package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Форма обновления продукта (все поля опциональны)")
public record ProductUpdateDto(
        @Size(min=3, max=32, message="от 3 до 32 символов")
        @Schema(description = "Новое название продукта (3–32 символа)", example = "Counter-Strike 2 Prime")
        String title,
        @Schema(description = "Новое описание продукта", example = "Обновлённое описание")
        String description,
        @Min(value=1, message="минимум 1₽")
        @Schema(description = "Новая цена в рублях (минимум 1)", example = "599")
        Integer price
) implements ProductInfo { }
