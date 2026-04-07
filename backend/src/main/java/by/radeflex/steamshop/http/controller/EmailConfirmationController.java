package by.radeflex.steamshop.http.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Email Confirmation API", description = "API для подтверждения email-адреса")
public interface EmailConfirmationController {
    @Operation(
            summary = "Подтвердить email",
            description = "Подтверждает email-адрес пользователя по токену из письма")
    @ApiResponse(responseCode = "200", description = "Email успешно подтверждён")
    @ApiResponse(responseCode = "404", description = "Токен не найден или истёк")
    ResponseEntity<?> confirmEmail(
            @Parameter(description = "UUID-токен подтверждения из письма") UUID token);

    @Operation(
            summary = "Отправить письмо подтверждения",
            description = "Повторно отправляет письмо с ссылкой подтверждения email текущему пользователю")
    @ApiResponse(responseCode = "200", description = "Письмо отправлено")
    @ApiResponse(responseCode = "403", description = "Отправка недоступна (например, cooldown или email уже подтверждён)")
    ResponseEntity<?> sendEmailConfirmation();
}
