package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ProductCreateEditDto(
        @NotNull(message="не может быть пустым")
        @Size(min=10, max=32, message="от 10 до 32 символов")
        String title,
        @NotNull(message="не может быть пустым")
        @Size(max=500, message="не более 500 символов")
        String description,
        @NotNull
        @Min(value=1, message="минимум 1₽")
        Integer price,
        @NotNull(message="укажите превью")
        @URL(message="должен быть формата URL")
        String previewUrl
) { }
