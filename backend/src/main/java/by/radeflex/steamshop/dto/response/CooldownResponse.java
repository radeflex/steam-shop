package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ об ограничении частоты запросов (cooldown)")
public record CooldownResponse(
        @Schema(description = "Тип ошибки", example = "cooldown")
        String error,
        @Schema(description = "Количество секунд до следующей попытки", example = "45")
        Long secondsLeft
) {
    public CooldownResponse(Long secondsLeft) {
        this("cooldown", secondsLeft);
    }
}
