package by.radeflex.steamshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AccountCreateDto(
    @NotNull
    String username,
    @NotNull
    String password,
    @NotNull
    @Email
    String email,
    @NotNull
    String emailPassword,
    @NotNull
    Integer productId
) {
}
