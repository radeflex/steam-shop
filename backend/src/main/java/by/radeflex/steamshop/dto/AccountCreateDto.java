package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Форма добавления Steam аккаунта")
public record AccountCreateDto(
    @NotNull
    @Schema(description = "Имя пользователя Steam",
            example = "example87",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String username,
    @NotNull
    @Schema(description = "Пароль Steam",
            example = "password",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String password,
    @NotNull
    @Email
    @Schema(description = "Email от аккаунта Steam",
            example = "example87@gmail.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String email,
    @NotNull
    @Schema(description = "Пароль от email",
            example = "emailPassword",
            requiredMode = Schema.RequiredMode.REQUIRED)
    String emailPassword,
    @NotNull
    @Schema(description = "ID продукта, который представлен в аккаунте",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    Integer productId
) {
}
