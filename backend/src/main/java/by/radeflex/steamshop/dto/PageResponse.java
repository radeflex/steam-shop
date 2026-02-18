package by.radeflex.steamshop.dto;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
@AllArgsConstructor(staticName = "of")
public class PageResponse<T> {
    List<T> content;
    Meta meta;

    public static <T> PageResponse<T> of(Page<T> page) {
        var content = page.getContent();
        var meta = new Meta(page.getNumber(), page.getSize(), page.getTotalPages());

        return new PageResponse<>(content, meta);
    }

    private record Meta(int pageNumber, int pageSize, int totalPages) {}
}
