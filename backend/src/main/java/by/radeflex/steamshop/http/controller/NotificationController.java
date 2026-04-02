package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.service.NotificationService;
import by.radeflex.steamshop.utils.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "API для работы с уведомлениями")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(
            summary = "Получить все уведомления (ADMIN)",
            description = "Возвращает страницу всех уведомлений в системе. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Список уведомлений")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<PageResponse<NotificationReadDto>> findAllAdmin(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findAllAdmin(pageable));
    }

    @Operation(
            summary = "Получить свои уведомления",
            description = "Возвращает страницу всех уведомлений текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список уведомлений")
    @GetMapping
    public ResponseEntity<PageResponse<NotificationReadDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findAll(pageable));
    }

    @Operation(
            summary = "Получить непрочитанные уведомления",
            description = "Возвращает страницу непрочитанных уведомлений текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Список непрочитанных уведомлений")
    @GetMapping("/unread")
    public ResponseEntity<PageResponse<NotificationReadDto>> findUnread(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findUnread(pageable));
    }

    @Operation(
            summary = "Отметить уведомление как прочитанное",
            description = "Помечает указанное уведомление прочитанным")
    @ApiResponse(responseCode = "200", description = "Уведомление отмечено прочитанным")
    @ApiResponse(responseCode = "404", description = "Уведомление не найдено")
    @PutMapping("{id}/read")
    public ResponseEntity<?> read(
            @Parameter(description = "ID уведомления", example = "1") @PathVariable Integer id) {
        if (!notificationService.read(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Отправить уведомление всем пользователям (ADMIN)",
            description = "Создаёт и рассылает уведомление всем пользователям. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Уведомление отправлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<NotificationReadDto> sendAll(@RequestBody @Valid NotificationCreateDto dto,
                                     BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(notificationService.sendAll(dto));
    }

    @Operation(
            summary = "Отправить уведомление конкретному пользователю (ADMIN)",
            description = "Создаёт и отправляет уведомление указанному пользователю. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Уведомление отправлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{userId}")
    public ResponseEntity<NotificationReadDto> sendToUser(
            @Parameter(description = "ID пользователя", example = "1") @PathVariable Integer userId,
            @RequestBody @Valid NotificationCreateDto dto,
            BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(notificationService.sendToUser(userId, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Удалить уведомление (ADMIN)",
            description = "Удаляет уведомление по ID. Только для администраторов")
    @ApiResponse(responseCode = "200", description = "Уведомление удалено")
    @ApiResponse(responseCode = "403", description = "Недостаточно прав")
    @ApiResponse(responseCode = "404", description = "Уведомление не найдено")
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID уведомления", example = "1") @PathVariable Integer id) {
        if (!notificationService.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(new MessageResponse("Notification deleted"));
    }
}
