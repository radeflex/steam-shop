package by.radeflex.steamshop.http.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Image API", description = "API для получения изображений")
public interface ImageController {
    @Operation(
            summary = "Получить изображение по UUID",
            description = "Возвращает изображение в формате PNG или JPG")
    @ApiResponse(responseCode = "200", description = "Изображение найдено и возвращено")
    @ApiResponse(responseCode = "404", description = "Изображение не найдено")
    ResponseEntity<byte[]> get(
            @Parameter(description = "UUID изображения", example = "550e8400-e29b-41d4-a716-446655440000")
            String uuid);
}
