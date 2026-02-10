package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.PageResponse;
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

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> findAllAdmin(Pageable pageable) {
        var page = notificationService.findAllAdmin(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }
    @GetMapping
    public ResponseEntity<?> findAll(Pageable pageable) {
        var page = notificationService.findAll(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> findUnread(Pageable pageable) {
        var page = notificationService.findUnread(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }

    @PutMapping("{id}/read")
    public ResponseEntity<?> read(@PathVariable Integer id) {
        if (!notificationService.read(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<?> sendAll(@RequestBody @Valid NotificationCreateDto dto,
                                     BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(notificationService.sendAll(dto));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{userId}")
    public ResponseEntity<?> sendToUser(@PathVariable Integer userId,
                                        @RequestBody @Valid NotificationCreateDto dto,
                                        BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(notificationService.sendToUser(userId, dto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!notificationService.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(Map.of("message", "Notification deleted"));
    }
}
