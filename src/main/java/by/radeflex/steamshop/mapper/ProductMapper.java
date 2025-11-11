package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductCreateEditDto;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    private void buildProduct(Product product, ProductCreateEditDto dto) {
        product.setTitle(dto.title());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setPreviewUrl(dto.previewUrl());
    }

    public Product mapFrom(Product old, ProductCreateEditDto dto) {
        buildProduct(old, dto);
        return old;
    }
    public Product mapFrom(ProductCreateEditDto dto) {
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
