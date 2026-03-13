package by.radeflex.steamshop.repository;

import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.filter.ProductFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<Product> findAllAvailable(ProductFilter filter, Pageable pageable);
}
