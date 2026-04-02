package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Schema(description = "Постраничный ответ")
public class PageResponse<T> {
    @Schema(description = "Список элементов на текущей странице")
    List<T> content;

    @Schema(description = "Метаданные пагинации")
    Meta meta;

    public static <T> PageResponse<T> of(Page<T> page) {
        var content = page.getContent();
        var meta = new Meta(page.getNumber(), page.getSize(), page.getTotalPages());
        return new PageResponse<>(content, meta);
    }

    @Schema(description = "Метаданные страницы")
    private record Meta(
            @Schema(description = "Номер текущей страницы (с нуля)", example = "0")
            int pageNumber,
            @Schema(description = "Размер страницы", example = "20")
            int pageSize,
            @Schema(description = "Всего страниц", example = "5")
            int totalPages
    ) {}
}
