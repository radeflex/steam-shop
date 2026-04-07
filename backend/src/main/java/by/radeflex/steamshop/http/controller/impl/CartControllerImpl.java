package by.radeflex.steamshop.http.controller.impl;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.http.controller.CartController;
import by.radeflex.steamshop.service.CartService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartControllerImpl implements CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<PageResponse<CartProductReadDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(cartService.findAll(pageable));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<CartProductReadDto> create(@PathVariable Integer productId) {
        var cartProduct = cartService.create(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var uri = URI.create("/cart/" + cartProduct.id());
        return ResponseEntity.created(uri).body(cartProduct);
    }

    @PutMapping("/{id}/quantity/{quantity}")
    public ResponseEntity<CartProductReadDto> updateQuantity(@PathVariable Integer id,
                                                             @PathVariable @Min(1) Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(id, quantity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> removeFromCart(@PathVariable Integer id) {
        if (!cartService.delete(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(new MessageResponse("Product removed from cart"));
    }
}
