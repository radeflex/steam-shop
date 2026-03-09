package by.radeflex.steamshop.dto;

import by.radeflex.steamshop.entity.UserRole;

public record AuthDto(
        Integer id,
        UserRole role
) {
}
