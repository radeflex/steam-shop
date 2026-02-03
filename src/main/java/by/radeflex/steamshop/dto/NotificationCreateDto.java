package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record NotificationCreateDto(
        @NotBlank(message="не может быть пустым")
        @Size(min=3, max=32, message="от 3 до 32 символов")
        String title,
        @NotBlank(message="не может быть пустым")
        @Size(max = 500, message = "текст уведомления не должен превышать 500 символов")
        String text
) {
}
