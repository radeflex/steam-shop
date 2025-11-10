package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PageResponse;
import by.radeflex.steamshop.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<?> findAll(Pageable pageable) {
        var page = cartService.findAll(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addToCart(@PathVariable Integer id) {
        if (!cartService.add(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(Map.of("message", "Product added"));
    }

    @PutMapping("/{productId}/quantity/{quantity}")
    public ResponseEntity<?> updateQuantity(@PathVariable Integer productId,
                                            @PathVariable @Min(1) Integer quantity) {
        if (!cartService.updateQuantity(productId, quantity))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(Map.of("message", "Product quantity updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer id) {
        if (!cartService.delete(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(Map.of("message", "Product removed"));
    }
}
