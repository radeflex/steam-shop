package by.radeflex.steamshop.dto;

import by.radeflex.steamshop.entity.AccountStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AccountReadDto(
        Integer id,
        Integer productId,
        AccountStatus status,
        Integer createdById,
        LocalDateTime createdAt
) {
}
