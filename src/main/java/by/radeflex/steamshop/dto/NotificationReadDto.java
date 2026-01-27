package by.radeflex.steamshop.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationReadDto(
        Integer id,
        String title,
        String text,
        String type,
        LocalDateTime createdAt
) {
}
