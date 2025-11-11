package by.radeflex.steamshop.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ProductHistoryReadDto(
    Integer id,
    Integer userId,
    Integer productId,
    String title,
    Integer price,
    Integer quantity,
    LocalDateTime createdAt
) {}
