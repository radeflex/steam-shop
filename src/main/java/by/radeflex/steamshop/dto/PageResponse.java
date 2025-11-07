package by.radeflex.steamshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class PageResponse<T> {
    private final List<T> content;
    private final Meta meta;

    public static <T> PageResponse<T> of(Page<T> page) {
        var content = page.getContent();
        var meta = new Meta(page.getNumber(), page.getSize(), page.getTotalPages());

        return new PageResponse<>(content, meta);
    }

    private record Meta(int pageNumber, int pageSize, int totalPages) {}
}
