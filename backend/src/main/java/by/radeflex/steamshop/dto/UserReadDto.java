package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Публичные данные пользователя")
public record UserReadDto(
        @Schema(description = "ID пользователя", example = "1")
        Integer id,
        @Schema(description = "Имя пользователя", example = "john_doe")
        String username,
        @Schema(description = "URL аватара", example = "550e8400-e29b-41d4-a716-446655440000")
        String avatarUrl,
        @Schema(description = "Дата регистрации")
        LocalDateTime createdAt
) {
}
