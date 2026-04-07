package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.ProductCreateDto;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.dto.ProductUpdateDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.filter.ProductFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Product API", description = "API для управления продуктами магазина")
public interface ProductController {
    @Operation(
            summary = "Получить список продуктов",
            description = "Возвращает страницу продуктов с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Список продуктов")
    ResponseEntity<PageResponse<ProductReadDto>> findAll(ProductFilter filter, Pageable pageable);

    @Operation(
            summary = "Создать новый продукт (ADMIN)",
            description = "Создаёт продукт с опциональным изображением. Только для администраторов")
    @ApiResponse(responseCode = "201", description = "Продукт создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    ResponseEntity<ProductReadDto> create(ProductCreateDto productCreateEditDto,
            BindingResult bindingResult,
            @Parameter(description = "Изображение продукта (PNG/JPG)") MultipartFile image);

    @Operation(
            summary = "Найти продукт по ID",
            description = "Возвращает данные одного продукта")
    @ApiResponse(responseCode = "200", description = "Продукт найден")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    ResponseEntity<ProductReadDto> findById(
            @Parameter(description = "ID продукта", example = "1") Integer id);

    @Operation(
            summary = "Обновить продукт (ADMIN)",
            description = "Обновляет данные продукта и/или его изображение. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Продукт обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    ResponseEntity<ProductReadDto> update(
            @Parameter(description = "ID продукта", example = "1") Integer id,
            ProductUpdateDto dto,
            BindingResult bindingResult,
            @Parameter(description = "Новое изображение продукта (PNG/JPG)")
            MultipartFile image);

    @Operation(
            summary = "Удалить продукт (ADMIN)",
            description = "Удаляет продукт по ID. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Продукт удалён")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID продукта", example = "1") Integer id);
}
