package by.radeflex.steamshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
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
