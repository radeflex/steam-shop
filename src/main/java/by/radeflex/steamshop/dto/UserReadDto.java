package by.radeflex.steamshop.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserReadDto(
        String username,
        String email,
        Integer balance,
        Integer points,
        LocalDateTime createdAt
) {
}
