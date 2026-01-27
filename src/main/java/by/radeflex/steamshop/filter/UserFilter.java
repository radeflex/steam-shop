package by.radeflex.steamshop.filter;

import java.time.LocalDateTime;

public record UserFilter(
        String username,
        LocalDateTime createdAt
) {
}
