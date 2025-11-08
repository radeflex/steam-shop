package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record UserCreateEditDto(
        @NotNull(message="укажите имя пользователя")
        @Size(min=3, max=32, message="должно быть от 3 до 32 символов")
        String username,
        @NotNull(message="укажите пароль")
        @Size(min=8, message="минимум 8 символов")
        String password,
        @NotNull(message="укажите email")
        @Email(message="некорректный email")
        String email
) {
}
