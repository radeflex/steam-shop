package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TopUpDto(
        @Min(value = 10, message = "Минимум 10₽")
        @NotNull
        Integer amount
) {
}
