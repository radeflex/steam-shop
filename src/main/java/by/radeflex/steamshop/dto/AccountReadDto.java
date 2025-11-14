package by.radeflex.steamshop.dto;

import lombok.Builder;

@Builder
public record AccountReadDto(
        Integer productId,
        String username,
        String password,
        String email,
        String emailPassword
) {}
