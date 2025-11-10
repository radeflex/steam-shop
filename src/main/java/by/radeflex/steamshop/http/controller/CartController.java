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

import java.net.URI;
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

    @PostMapping("/{productId}")
    public ResponseEntity<?> addToCart(@PathVariable Integer productId) {
        var product = cartService.add(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var uri = URI.create("/cart/" + product.id());
        return ResponseEntity.created(uri).body(product);
    }

    @PutMapping("/{productId}/quantity/{quantity}")
    public ResponseEntity<?> updateQuantity(@PathVariable Integer productId,
                                            @PathVariable @Min(1) Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(productId, quantity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer id) {
        if (!cartService.delete(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(Map.of("message", "Product removed from cart"));
    }
}
