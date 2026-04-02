package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с JWT-токеном")
public record JwtResponse(
        @Schema(description = "JWT-токен для авторизации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {
}
