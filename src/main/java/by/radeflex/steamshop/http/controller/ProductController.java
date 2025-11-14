package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.OrderDto;
import by.radeflex.steamshop.dto.PageResponse;
import by.radeflex.steamshop.dto.ProductCreateEditDto;
import by.radeflex.steamshop.filter.ProductFilter;
import by.radeflex.steamshop.repository.ProductRepository;
import by.radeflex.steamshop.repository.UserProductHistoryRepository;
import by.radeflex.steamshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static by.radeflex.steamshop.validation.ValidationUtils.checkErrors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final UserProductHistoryRepository userProductHistoryRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> findAll(ProductFilter filter, Pageable pageable) {
        var page = productService.findAll(filter, pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ProductCreateEditDto productCreateEditDto,
                                    BindingResult bindingResult) {
        checkErrors(bindingResult);
        var product = productService.create(productCreateEditDto);
        var uri = URI.create("/products/" + product.id());
        return ResponseEntity.created(uri).body(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @RequestBody @Valid ProductCreateEditDto dto,
                                    BindingResult bindingResult) {
        checkErrors(bindingResult);
        return ResponseEntity.ok(productService.update(id, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!productService.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(Map.of("message", "Product deleted"));
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> buyProducts(@RequestBody @Valid List<OrderDto> orderDtos) {
        return ResponseEntity.ok(productService.purchase(orderDtos));
    }
}
