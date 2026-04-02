package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.With;

@With
@Schema(description = "Форма обновления профиля пользователя (все поля опциональны)")
public record UserUpdateDto(
        @Size(min=3, max=32, message="должно быть от 3 до 32 символов")
        @Schema(description = "Новое имя пользователя (3–32 символа)", example = "john_updated")
        String username,
        @Size(min=8, message="минимум 8 символов")
        @Schema(description = "Новый пароль (минимум 8 символов)", example = "newSecurePass123")
        String password,
        @Email(message="некорректный email")
        @Schema(description = "Новый email пользователя", example = "new_john@example.com")
        String email
) implements UserInfo {
}
