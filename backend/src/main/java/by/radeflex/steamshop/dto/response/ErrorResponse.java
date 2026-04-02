package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с сообщением об ошибке")
public record ErrorResponse(
        @Schema(description = "Описание ошибки", example = "Resource not found")
        String error
) {
}
