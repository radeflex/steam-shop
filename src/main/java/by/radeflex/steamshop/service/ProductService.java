package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.*;
import by.radeflex.steamshop.entity.Product;
import by.radeflex.steamshop.entity.QProduct;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.filter.PredicateBuilder;
import by.radeflex.steamshop.filter.ProductFilter;
import by.radeflex.steamshop.mapper.AccountMapper;
import by.radeflex.steamshop.mapper.ProductHistoryMapper;
import by.radeflex.steamshop.mapper.ProductMapper;
import by.radeflex.steamshop.repository.AccountRepository;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final ProductHistoryMapper productHistoryMapper;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ImageService imageService;

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

    private Product uploadImage(MultipartFile file, Product p) {
        if (file != null) {
            if (!p.getPreviewUrl().isBlank())
                imageService.delete(p.getPreviewUrl());
            var url = imageService.upload(file);
            p.setPreviewUrl(url);
        }
        return p;
    }

    @Transactional
    public ProductReadDto create(ProductCreateEditDto dto, MultipartFile file) {
        checkUnique(dto);
        return Optional.of(dto)
                .map(productMapper::mapFrom)
                .map(productRepository::save)
                .map(p -> uploadImage(file, p))
                .map(productMapper::mapFrom)
                .orElseThrow();
    }

    @Transactional
    public Optional<ProductReadDto> update(Integer id, ProductCreateEditDto dto,
                                           MultipartFile file) {
        checkUnique(dto);
        return productRepository.findById(id)
                .map(p -> uploadImage(file, p))
                .map(p -> productMapper.mapFrom(p, dto))
                .map(productMapper::mapFrom);
    }

    @Transactional
    public boolean delete(Integer id) {
        return productRepository.findById(id)
                .map(p -> {
                    imageService.delete(p.getPreviewUrl());
                    productRepository.delete(p);
                    return true;
                }).orElse(false);
    }

    @Transactional
    public List<AccountReadDto> purchase(List<OrderDto> orderDtos) {
        return orderDtos.stream()
                .map(o -> Map.entry(productRepository.findById(o.productId()), o.quantity()))
                .filter(e -> e.getKey().isPresent())
                .map(e -> accountRepository.findAccounts(e.getKey().get().getId(), e.getValue()))
                .filter(l -> !l.isEmpty())
                .peek(l -> {
                    var p = l.get(0).getProduct();
                    l.forEach(a -> accountRepository.save(a.toggleAvailable()));
                    userProductHistoryRepository.save(
                            productHistoryMapper.mapFrom(p, l.size())
                    );
                })
                .flatMap(List::stream)
                .map(accountMapper::mapFrom)
                .toList();
    }
}
