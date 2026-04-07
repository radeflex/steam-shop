package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.CartProductReadDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart API", description = "API для управления корзиной пользователя")
public interface CartController {
    @Operation(
            summary = "Получить содержимое корзины",
            description = "Возвращает страницу товаров в корзине текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список товаров в корзине")
    ResponseEntity<PageResponse<CartProductReadDto>> findAll(Pageable pageable);

    @Operation(
            summary = "Добавить товар в корзину",
            description = "Добавляет указанный продукт в корзину текущего пользователя")
    @ApiResponse(responseCode = "201", description = "Товар добавлен в корзину")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    ResponseEntity<CartProductReadDto> create(
            @Parameter(description = "ID продукта", example = "1") Integer productId);

    @Operation(
            summary = "Изменить количество товара в корзине",
            description = "Обновляет количество указанной позиции в корзине")
    @ApiResponse(responseCode = "200", description = "Количество обновлено")
    @ApiResponse(responseCode = "404", description = "Позиция в корзине не найдена")
    ResponseEntity<CartProductReadDto> updateQuantity(
            @Parameter(description = "ID позиции в корзине", example = "1") @PathVariable Integer id,
            @Parameter(description = "Новое количество (минимум 1)", example = "2") Integer quantity);

    @Operation(
            summary = "Удалить товар из корзины",
            description = "Удаляет позицию из корзины по её ID")
    @ApiResponse(responseCode = "200", description = "Товар удалён из корзины")
    @ApiResponse(responseCode = "404", description = "Позиция в корзине не найдена")
    ResponseEntity<MessageResponse> removeFromCart(
            @Parameter(description = "ID позиции в корзине", example = "1") Integer id);
}
