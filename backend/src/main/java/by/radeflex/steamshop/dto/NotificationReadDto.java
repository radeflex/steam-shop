package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Данные уведомления")
public record NotificationReadDto(
        @Schema(description = "ID уведомления", example = "1")
        Integer id,
        @Schema(description = "Заголовок уведомления", example = "Важное обновление")
        String title,
        @Schema(description = "Текст уведомления", example = "В магазине появились новые товары!")
        String text,
        @Schema(description = "Тип уведомления", example = "INFO")
        String type,
        @Schema(description = "Дата создания")
        LocalDateTime createdAt
) {
}
