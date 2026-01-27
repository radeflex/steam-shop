package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ProductUpdateDto(
        @Size(min=3, max=32, message="от 3 до 32 символов")
        String title,
        @Size(max=500, message="не более 500 символов")
        String description,
        @Min(value=1, message="минимум 1₽")
        Integer price
) implements ProductInfo { }