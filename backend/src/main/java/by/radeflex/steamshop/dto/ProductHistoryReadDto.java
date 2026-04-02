package by.radeflex.steamshop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Запись истории покупки продукта")
public record ProductHistoryReadDto(
        @Schema(description = "ID записи", example = "1")
        Integer id,
        @Schema(description = "ID пользователя", example = "3")
        Integer userId,
        @Schema(description = "ID продукта", example = "5")
        Integer productId,
        @Schema(description = "Название продукта на момент покупки", example = "Counter-Strike 2")
        String title,
        @Schema(description = "Цена на момент покупки (в рублях)", example = "499")
        Integer price,
        @Schema(description = "Купленное количество", example = "1")
        Integer quantity,
        @Schema(description = "Дата покупки")
        LocalDateTime createdAt
) {}
