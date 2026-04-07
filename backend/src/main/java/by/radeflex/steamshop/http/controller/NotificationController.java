package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

@Tag(name = "Notification API", description = "API для работы с уведомлениями")
public interface NotificationController {

    @Operation(
            summary = "Получить все уведомления (ADMIN)",
            description = "Возвращает страницу всех уведомлений в системе. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Список уведомлений")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    ResponseEntity<PageResponse<NotificationReadDto>> findAllAdmin(Pageable pageable);

    @Operation(
            summary = "Получить свои уведомления",
            description = "Возвращает страницу всех уведомлений текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список уведомлений")
    ResponseEntity<PageResponse<NotificationReadDto>> findAll(Pageable pageable);

    @Operation(
            summary = "Получить непрочитанные уведомления",
            description = "Возвращает страницу непрочитанных уведомлений текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список непрочитанных уведомлений")
    ResponseEntity<PageResponse<NotificationReadDto>> findUnread(Pageable pageable);

    @Operation(
            summary = "Отметить уведомление как прочитанное",
            description = "Помечает указанное уведомление прочитанным")
    @ApiResponse(responseCode = "200", description = "Уведомление отмечено прочитанным")
    @ApiResponse(responseCode = "404", description = "Уведомление не найдено")
    ResponseEntity<?> read(
            @Parameter(description = "ID уведомления", example = "1") Integer id);

    @Operation(
            summary = "Отправить уведомление всем пользователям (ADMIN)",
            description = "Создаёт и рассылает уведомление всем пользователям. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Уведомление отправлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    ResponseEntity<NotificationReadDto> sendAll(NotificationCreateDto dto,
                                                       BindingResult bindingResult);

    @Operation(
            summary = "Отправить уведомление конкретному пользователю (ADMIN)",
            description = "Создаёт и отправляет уведомление указанному пользователю. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Уведомление отправлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    ResponseEntity<NotificationReadDto> sendToUser(
            @Parameter(description = "ID пользователя", example = "1") Integer userId,
            NotificationCreateDto dto,
            BindingResult bindingResult);

    @Operation(
            summary = "Удалить уведомление (ADMIN)",
            description = "Удаляет уведомление по ID. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Уведомление удалено")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Уведомление не найдено")
    ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID уведомления", example = "1") Integer id);
}
