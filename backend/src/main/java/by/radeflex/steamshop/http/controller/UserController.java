package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.CurrentUserReadDto;
import by.radeflex.steamshop.dto.ProductHistoryReadDto;
import by.radeflex.steamshop.dto.UserReadDto;
import by.radeflex.steamshop.dto.UserUpdateDto;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.filter.UserFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User API", description = "API для управления пользователями")
public interface UserController {
    @Operation(
            summary = "Получить список всех пользователей (ADMIN)",
            description = "Возвращает страницу пользователей с возможностью фильтрации. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Список пользователей")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    ResponseEntity<PageResponse<UserReadDto>> findAll(UserFilter filter, Pageable pageable);

    @Operation(
            summary = "Обновить профиль текущего пользователя",
            description = "Обновляет данные и/или аватар текущего аутентифицированного пользователя")
    @ApiResponse(responseCode = "200", description = "Профиль обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    ResponseEntity<CurrentUserReadDto> update(
            UserUpdateDto userUpdateDto,
            BindingResult bindingResult,
            @Parameter(description = "Новый аватар пользователя (PNG/JPG)")
            MultipartFile image);

    @Operation(
            summary = "Получить текущего пользователя",
            description = "Возвращает данные аутентифицированного пользователя")
    @ApiResponse(responseCode = "200", description = "Данные текущего пользователя")
    ResponseEntity<CurrentUserReadDto> getCurrentUser();

    @Operation(
            summary = "Найти пользователя по ID",
            description = "Возвращает публичные данные пользователя по его ID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    ResponseEntity<UserReadDto> findById(@Parameter(description = "ID пользователя", example = "1") Integer id);

    @Operation(
            summary = "Удалить аватар текущего пользователя",
            description = "Сбрасывает аватар текущего пользователя на стандартный")
    @ApiResponse(responseCode = "200", description = "Аватар удалён")
    ResponseEntity<?> deleteCurrentUserAvatar();

    @Operation(
            summary = "История покупок текущего пользователя",
            description = "Возвращает страницу истории купленных продуктов текущего пользователя")
    @ApiResponse(responseCode = "200", description = "История покупок")
    ResponseEntity<PageResponse<ProductHistoryReadDto>> getCurrentUserProductHistory(Pageable pageable);
}
