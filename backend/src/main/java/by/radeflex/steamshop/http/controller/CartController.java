package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cart API", description = "API для управления корзиной пользователя")
public class CartController {
    private final CartService cartService;

    @Operation(
            summary = "Получить содержимое корзины",
            description = "Возвращает страницу товаров в корзине текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список товаров в корзине")
    @GetMapping
    public ResponseEntity<PageResponse<CartProductReadDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(cartService.findAll(pageable));
    }

    @Operation(
            summary = "Добавить товар в корзину",
            description = "Добавляет указанный продукт в корзину текущего пользователя")
    @ApiResponse(responseCode = "201", description = "Товар добавлен в корзину")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @PostMapping("/{productId}")
    public ResponseEntity<CartProductReadDto> create(
            @Parameter(description = "ID продукта", example = "1") @PathVariable Integer productId) {
        var cartProduct = cartService.create(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var uri = URI.create("/cart/" + cartProduct.id());
        return ResponseEntity.created(uri).body(cartProduct);
    }

    @Operation(
            summary = "Изменить количество товара в корзине",
            description = "Обновляет количество указанной позиции в корзине")
    @ApiResponse(responseCode = "200", description = "Количество обновлено")
    @ApiResponse(responseCode = "404", description = "Позиция в корзине не найдена")
    @PutMapping("/{id}/quantity/{quantity}")
    public ResponseEntity<CartProductReadDto> updateQuantity(
            @Parameter(description = "ID позиции в корзине", example = "1") @PathVariable Integer id,
            @Parameter(description = "Новое количество (минимум 1)", example = "2") @PathVariable @Min(1) Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(id, quantity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Удалить товар из корзины",
            description = "Удаляет позицию из корзины по её ID")
    @ApiResponse(responseCode = "200", description = "Товар удалён из корзины")
    @ApiResponse(responseCode = "404", description = "Позиция в корзине не найдена")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> removeFromCart(
            @Parameter(description = "ID позиции в корзине", example = "1") @PathVariable Integer id) {
        if (!cartService.delete(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(new MessageResponse("Product removed from cart"));
    }
}
