package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.service.EmailConfirmationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Email Confirmation API", description = "API для подтверждения email-адреса")
public class EmailConfirmationController {
    private final EmailConfirmationService emailConfirmationService;

    @Operation(
            summary = "Подтвердить email",
            description = "Подтверждает email-адрес пользователя по токену из письма")
    @ApiResponse(responseCode = "200", description = "Email успешно подтверждён")
    @ApiResponse(responseCode = "404", description = "Токен не найден или истёк")
    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(
            @Parameter(description = "UUID-токен подтверждения из письма") @RequestParam UUID token) {
        if (!emailConfirmationService.confirmEmail(token))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Отправить письмо подтверждения",
            description = "Повторно отправляет письмо с ссылкой подтверждения email текущему пользователю")
    @ApiResponse(responseCode = "200", description = "Письмо отправлено")
    @ApiResponse(responseCode = "403", description = "Отправка недоступна (например, cooldown или email уже подтверждён)")
    @PostMapping("/send-email-confirmation")
    public ResponseEntity<?> sendEmailConfirmation() {
        if (!emailConfirmationService.sendEmailConfirmation())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return ResponseEntity.ok().build();
    }
}
