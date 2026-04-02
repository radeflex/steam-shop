package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Форма создания уведомления")
public record NotificationCreateDto(
        @NotBlank(message="не может быть пустым")
        @Size(min=3, max=32, message="от 3 до 32 символов")
        @Schema(description = "Заголовок уведомления (3–32 символа)", example = "Важное обновление",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotBlank(message="не может быть пустым")
        @Size(max = 500, message = "текст уведомления не должен превышать 500 символов")
        @Schema(description = "Текст уведомления (до 500 символов)", example = "В магазине появились новые товары!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String text
) {
}
