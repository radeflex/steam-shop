package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderDto(
        @NotNull
        Integer productId,
        @NotNull
        @Min(1)
        Integer quantity
 ) {
}
