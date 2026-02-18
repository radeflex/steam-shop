package by.radeflex.steamshop.dto;

import java.util.List;

public record CsvResponseDto(
        Integer total,
        Integer inserted,
        List<Integer> errorRows
) {
}
