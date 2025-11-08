package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.ProductCreateEditDto;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.entity.QProduct;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.filter.PredicateBuilder;
import by.radeflex.steamshop.filter.ProductFilter;
import by.radeflex.steamshop.mapper.ProductMapper;
import by.radeflex.steamshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private void checkUnique(ProductCreateEditDto dto) {
        List<String> existing = new ArrayList<>();
        if (productRepository.exists(QProduct.product.title.eq(dto.title())))
            existing.add("title");
        if (productRepository.exists(QProduct.product.description.eq(dto.description())))
            existing.add("description");
        if (!existing.isEmpty())
            throw new ObjectExistsException(existing);
    }

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
        checkUnique(dto);
        return Optional.of(dto)
                .map(productMapper::mapFrom)
                .map(productRepository::save)
                .map(productMapper::mapFrom)
                .orElseThrow();
    }

    @Transactional
    public Optional<ProductReadDto> update(Integer id, ProductCreateEditDto dto) {
        checkUnique(dto);
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
