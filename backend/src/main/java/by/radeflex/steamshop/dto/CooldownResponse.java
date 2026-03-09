package by.radeflex.steamshop.dto;

public record CooldownResponse(
        String error,
        Long secondsLeft
) {
}
