package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Ответ с ошибками валидации полей")
public record ValidationErrorResponse(
        @Schema(description = "Тип ошибки", example = "Validation error")
        String error,
        @Schema(description = "Карта ошибок: имя поля → сообщение об ошибке",
                example = "{\"username\": \"должно быть от 3 до 32 символов\"}")
        Map<String, String> errors
) {
    public ValidationErrorResponse(Map<String, String> errors) {
        this("Validation error", errors);
    }
}
