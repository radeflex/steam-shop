package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Форма входа в систему")
public record LoginDto(
        @NotNull
        @Schema(description = "Имя пользователя", example = "john_doe",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String username,
        @NotNull
        @Schema(description = "Пароль", example = "securePass123",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
