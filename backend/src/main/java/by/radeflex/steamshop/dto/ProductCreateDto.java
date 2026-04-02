package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Форма создания продукта")
public record ProductCreateDto(
        @NotBlank(message="не может быть пустым")
        @Size(min=3, max=32, message="от 3 до 32 символов")
        @Schema(description = "Название продукта (3–32 символа)", example = "Counter-Strike 2",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotBlank(message="не может быть пустым")
        @Size(max=500, message="не более 500 символов")
        @Schema(description = "Описание продукта (до 500 символов)", example = "Популярный тактический шутер от Valve",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String description,
        @NotNull
        @Min(value=1, message="минимум 1₽")
        @Schema(description = "Цена в рублях (минимум 1)", example = "499",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer price
) implements ProductInfo { }
