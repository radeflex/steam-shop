package by.radeflex.steamshop.dto;

import by.radeflex.steamshop.entity.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Данные Steam аккаунта")
public record AccountReadDto(
        @Schema(description = "ID аккаунта",
                example = "1")
        Integer id,
        @Schema(description = "ID продукта",
                example = "1")
        Integer productId,
        @Schema(description = "Статус аккаунта")
        AccountStatus status,
        @Schema(description = "ID создателя аккаунта",
                example = "1")
        Integer createdById,
        @Schema(description = "Дата добавления")
        LocalDateTime createdAt
) {
}
