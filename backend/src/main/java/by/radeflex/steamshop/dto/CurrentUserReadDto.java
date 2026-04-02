package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Данные текущего аутентифицированного пользователя")
public record CurrentUserReadDto(
        @Schema(description = "ID пользователя", example = "1")
        Integer id,
        @Schema(description = "Имя пользователя", example = "john_doe")
        String username,
        @Schema(description = "Email пользователя", example = "john@example.com")
        String email,
        @Schema(description = "Баланс в рублях", example = "1500")
        Integer balance,
        @Schema(description = "Количество баллов лояльности", example = "200")
        Integer points,
        @Schema(description = "Дата регистрации")
        LocalDateTime createdAt,
        @Schema(description = "Роль пользователя", example = "USER")
        String role,
        @Schema(description = "URL аватара", example = "/files/550e8400-e29b-41d4-a716-446655440000")
        String avatarUrl,
        @Schema(description = "Подтверждён ли email", example = "true")
        Boolean confirmed
) {
}
