package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductCreateDto;
import by.radeflex.steamshop.dto.ProductUpdateDto;
import by.radeflex.steamshop.dto.ProductInfo;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    private void buildProduct(Product product, ProductInfo dto) {
        if (dto.title() != null) product.setTitle(dto.title());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.price() != null) product.setPrice(dto.price());
    }

    public Product mapFrom(Product old, ProductUpdateDto dto) {
        buildProduct(old, dto);
        return old;
    }
    public Product mapFrom(ProductCreateDto dto) {
        Product product = new Product();
        buildProduct(product, dto);
        return product;
    }
    public ProductReadDto mapFrom(Product product) {
        return ProductReadDto.builder()
                .id(product.getId())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .previewUrl(product.getPreviewUrl())
                .build();
    }
}
