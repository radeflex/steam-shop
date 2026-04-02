package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.UserUpdateDto;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.filter.UserFilter;
import by.radeflex.steamshop.service.UserService;
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

import static by.radeflex.steamshop.utils.ValidationUtils.checkErrors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User API", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Получить список всех пользователей (ADMIN)",
            description = "Возвращает страницу пользователей с возможностью фильтрации. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll(UserFilter filter, Pageable pageable) {
        var page = PageResponse.of(userService.findAll(filter, pageable));
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Обновить профиль текущего пользователя",
            description = "Обновляет данные и/или аватар текущего аутентифицированного пользователя")
    @ApiResponse(responseCode = "200", description = "Профиль обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PutMapping(value = "/current", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @RequestPart(value = "data", required = false) @Valid UserUpdateDto userUpdateDto,
            BindingResult bindingResult,
            @Parameter(description = "Новый аватар пользователя (PNG/JPG)")
            @RequestPart(value = "image", required = false) MultipartFile image) {
        checkErrors(bindingResult);
        return ResponseEntity.ok(userService.update(userUpdateDto, image)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Получить текущего пользователя",
            description = "Возвращает данные аутентифицированного пользователя")
    @ApiResponse(responseCode = "200", description = "Данные текущего пользователя")
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(userService.findCurrent());
    }

    @Operation(
            summary = "Найти пользователя по ID",
            description = "Возвращает публичные данные пользователя по его ID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(
            @Parameter(description = "ID пользователя", example = "1") @PathVariable Integer id) {
        return ResponseEntity.ok(userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Удалить аватар текущего пользователя",
            description = "Сбрасывает аватар текущего пользователя на стандартный")
    @ApiResponse(responseCode = "200", description = "Аватар удалён")
    @DeleteMapping("/current/avatar")
    public ResponseEntity<?> deleteCurrentUserAvatar() {
        userService.resetAvatar();
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "История покупок текущего пользователя",
            description = "Возвращает страницу истории купленных продуктов текущего пользователя")
    @ApiResponse(responseCode = "200", description = "История покупок")
    @GetMapping("/current/product-history")
    public ResponseEntity<?> getCurrentUserProductHistory(Pageable pageable) {
        return ResponseEntity.ok(userService.getProductHistoryCurrent(pageable));
    }
}
