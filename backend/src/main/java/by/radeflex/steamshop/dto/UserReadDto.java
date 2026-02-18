package by.radeflex.steamshop.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserReadDto(
        Integer id,
        String username,
        String avatarUrl,
        LocalDateTime createdAt
) {
}
