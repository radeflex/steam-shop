package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
@Tag(name = "Image API", description = "API для получения изображений")
public class ImageController {
    private final ImageService imageService;

    @Operation(
            summary = "Получить изображение по UUID",
            description = "Возвращает изображение в формате PNG или JPG")
    @ApiResponse(responseCode = "200", description = "Изображение найдено и возвращено")
    @ApiResponse(responseCode = "404", description = "Изображение не найдено")
    @GetMapping(value = "/{uuid}", produces = {"image/png", "image/jpg"})
    public ResponseEntity<byte[]> get(
            @Parameter(description = "UUID изображения", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String uuid) {
        return ResponseEntity.ok(imageService.get(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
