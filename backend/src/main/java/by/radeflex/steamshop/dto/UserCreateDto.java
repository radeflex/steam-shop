package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.With;

@With
@Schema(description = "Форма регистрации нового пользователя")
public record UserCreateDto(
        @NotNull(message="укажите имя пользователя")
        @Size(min=3, max=32, message="должно быть от 3 до 32 символов")
        @Schema(description = "Имя пользователя (3–32 символа)", example = "john_doe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @NotNull(message="укажите пароль")
        @Size(min=8, message="минимум 8 символов")
        @Schema(description = "Пароль (минимум 8 символов)", example = "securePass123",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String password,
        @NotNull(message="укажите email")
        @Email(message="некорректный email")
        @Schema(description = "Email пользователя", example = "john@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String email
) implements UserInfo {
}
