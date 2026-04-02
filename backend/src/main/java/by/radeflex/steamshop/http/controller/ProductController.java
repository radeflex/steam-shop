package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.ProductCreateDto;
import by.radeflex.steamshop.dto.ProductReadDto;
import by.radeflex.steamshop.dto.ProductUpdateDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.filter.ProductFilter;
import by.radeflex.steamshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static by.radeflex.steamshop.utils.ValidationUtils.checkErrors;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "API для управления продуктами магазина")
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "Получить список продуктов",
            description = "Возвращает страницу продуктов с возможностью фильтрации")
    @ApiResponse(responseCode = "200", description = "Список продуктов")
    @GetMapping
    public ResponseEntity<PageResponse<ProductReadDto>> findAll(ProductFilter filter, Pageable pageable) {
        return ResponseEntity.ok(productService.findAll(filter, pageable));
    }

    @Operation(
            summary = "Создать новый продукт (ADMIN)",
            description = "Создаёт продукт с опциональным изображением. Только для администраторов")
    @ApiResponse(responseCode = "201", description = "Продукт создан")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductReadDto> create(
            @RequestPart("data") @Valid ProductCreateDto productCreateEditDto,
            BindingResult bindingResult,
            @Parameter(description = "Изображение продукта (PNG/JPG)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        checkErrors(bindingResult);
        var product = productService.create(productCreateEditDto, image);
        var uri = URI.create("/products/" + product.id());
        return ResponseEntity.created(uri).body(product);
    }

    @Operation(
            summary = "Найти продукт по ID",
            description = "Возвращает данные одного продукта")
    @ApiResponse(responseCode = "200", description = "Продукт найден")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @GetMapping("/{id}")
    public ResponseEntity<ProductReadDto> findById(
            @Parameter(description = "ID продукта", example = "1") @PathVariable Integer id) {
        return ResponseEntity.ok(productService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Обновить продукт (ADMIN)",
            description = "Обновляет данные продукта и/или его изображение. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Продукт обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductReadDto> update(
            @Parameter(description = "ID продукта", example = "1") @PathVariable Integer id,
            @RequestPart(value = "data", required = false) @Valid ProductUpdateDto dto,
            BindingResult bindingResult,
            @Parameter(description = "Новое изображение продукта (PNG/JPG)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        checkErrors(bindingResult);
        return ResponseEntity.ok(productService.update(id, dto, image)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Удалить продукт (ADMIN)",
            description = "Удаляет продукт по ID. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Продукт удалён")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID продукта", example = "1") @PathVariable Integer id) {
        if (!productService.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(new MessageResponse("Product deleted"));
    }
}
