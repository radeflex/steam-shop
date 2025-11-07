package by.radeflex.steamshop.dto;

public record ProductCreateEditDto(
        String title,
        String description,
        Integer price,
        String previewUrl
) { }
