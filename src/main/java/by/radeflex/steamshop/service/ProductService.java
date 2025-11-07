package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.ProductCreateEditDto;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.entity.QProduct;
import by.radeflex.steamshop.exception.ProductExistsException;
import by.radeflex.steamshop.filter.PredicateBuilder;
import by.radeflex.steamshop.filter.ProductFilter;
import by.radeflex.steamshop.mapper.ProductMapper;
import by.radeflex.steamshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductReadDto> findAll(ProductFilter filter, Pageable pageable) {
        var predicate = PredicateBuilder.builder()
                .add(filter.title(), QProduct.product.title::containsIgnoreCase)
                .add(filter.priceMin(), QProduct.product.price::goe)
                .add(filter.priceMax(), QProduct.product.price::loe)
                .buildAnd();
        if (predicate == null) {
            return productRepository.findAll(pageable)
                    .map(productMapper::mapFrom);
        }
        return productRepository.findAll(predicate, pageable)
                .map(productMapper::mapFrom);
    }

    public Optional<ProductReadDto> findById(Integer id) {
        return productRepository.findById(id).map(productMapper::mapFrom);
    }

    @Transactional
    public ProductReadDto save(ProductCreateEditDto dto) {
        if (productRepository.exists(QProduct.product.title.eq(dto.title())))
            throw new ProductExistsException("Товар с таким названием уже есть");
        if (productRepository.exists(QProduct.product.description.eq(dto.description())))
            throw new ProductExistsException("Товар с таким описанием уже есть");
        return Optional.of(dto)
                .map(productMapper::mapFrom)
                .map(productRepository::save)
                .map(productMapper::mapFrom)
                .orElseThrow();
    }

    @Transactional
    public Optional<ProductReadDto> update(Integer id, ProductCreateEditDto dto) {
        return productRepository.findById(id)
                .map(p -> productMapper.mapFrom(p, dto))
                .map(productMapper::mapFrom);
    }

    @Transactional
    public boolean delete(Integer id) {
        return productRepository.findById(id)
                .map(p -> {
                    productRepository.delete(p);
                    return true;
                }).orElse(false);
    }
}
