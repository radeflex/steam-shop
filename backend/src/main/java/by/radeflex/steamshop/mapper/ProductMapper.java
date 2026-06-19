package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.ProductAdminReadDto;
import by.radeflex.steamshop.dto.ProductInfo;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.entity.Product;
import com.querydsl.core.Tuple;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @BeanMapping(nullValuePropertyMappingStrategy =
            NullValuePropertyMappingStrategy.IGNORE)
    Product map(@MappingTarget Product old, ProductInfo dto);

    default Product map(ProductInfo dto) {
        return map(new Product(), dto);
    }

    @Mapping(source = "previewUrl", target = "previewUrl", defaultValue = "no-image")
    ProductReadDto map(Product product);
    default ProductAdminReadDto map(Product product, Long left) {
        var dto = mapAdmin(product);
        return dto.withLeft(left);
    }

    @Mapping(source = "previewUrl", target = "previewUrl", defaultValue = "no-image")
    ProductAdminReadDto mapAdmin(Product product);
    default ProductAdminReadDto map(Tuple tuple) {
        var product = tuple.get(0, Product.class);
        var left = tuple.get(1, Long.class);
        return map(product, left);
    }
}
