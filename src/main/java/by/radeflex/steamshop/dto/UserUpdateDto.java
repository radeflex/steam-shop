package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record UserUpdateDto(
        @Size(min=3, max=32, message="должно быть от 3 до 32 символов")
        String username,
        @Size(min=8, message="минимум 8 символов")
        String password,
        @Email(message="некорректный email")
        String email
) implements UserInfo {
}
