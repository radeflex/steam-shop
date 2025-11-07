package by.radeflex.steamshop.dto;

import lombok.Builder;

@Builder
public record ProductReadDto(
        Integer id,
        String title,
        String description,
        Integer price,
        String previewUrl
) {
}
