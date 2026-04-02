package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Форма пополнения баланса")
public record TopUpDto(
        @Min(value = 10, message = "Минимум 10₽")
        @NotNull
        @Schema(description = "Сумма пополнения в рублях (минимум 10)", example = "500",
                requiredMode = Schema.RequiredMode.REQUIRED)
        Integer amount
) {
}
