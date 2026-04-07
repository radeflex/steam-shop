package by.radeflex.steamshop.http.controller.impl;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.dto.response.MessageResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.http.controller.NotificationController;
import by.radeflex.steamshop.service.NotificationService;
import by.radeflex.steamshop.utils.ValidationUtils;
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
public class NotificationControllerImpl implements NotificationController {
    private final NotificationService notificationService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<PageResponse<NotificationReadDto>> findAllAdmin(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findAllAdmin(pageable));
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationReadDto>> findAll(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findAll(pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<PageResponse<NotificationReadDto>> findUnread(Pageable pageable) {
        return ResponseEntity.ok(notificationService.findUnread(pageable));
    }

    @PutMapping("{id}/read")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        if (!notificationService.read(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<NotificationReadDto> sendAll(@RequestBody @Valid NotificationCreateDto dto,
                                     BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(notificationService.sendAll(dto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{userId}")
    public ResponseEntity<NotificationReadDto> sendToUser(@PathVariable Integer userId,
            @RequestBody @Valid NotificationCreateDto dto,
            BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(notificationService.sendToUser(userId, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Integer id) {
        if (!notificationService.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(new MessageResponse("Notification deleted"));
    }
}
