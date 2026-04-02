package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с текстовым сообщением")
public record MessageResponse(
        @Schema(description = "Сообщение", example = "Operation completed successfully")
        String message
) {
}
