package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Ответ на добавление аккаунтов из CSV")
public record CsvResponse(
        @Schema(description = "Рассмотренных аккаунтов")
        Integer total,
        @Schema(description = "Добавленных аккаунтов")
        Integer inserted,
        @Schema(description = "Номера ошибочных CSV строк")
        List<Integer> errorRows
) {
}
