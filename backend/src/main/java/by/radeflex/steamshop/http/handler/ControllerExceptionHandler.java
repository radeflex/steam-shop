package by.radeflex.steamshop.http.handler;

import by.radeflex.steamshop.dto.response.CooldownResponse;
import by.radeflex.steamshop.dto.response.ErrorResponse;
import by.radeflex.steamshop.dto.response.ValidationErrorResponse;
import by.radeflex.steamshop.exception.AccountLackException;
import by.radeflex.steamshop.exception.EmailCooldownException;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.exception.ValidationError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ValidationError.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(ValidationError ex) {
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getErrors()));
    }
    @ExceptionHandler(ObjectExistsException.class)
    public ResponseEntity<?> handleExistsException(ObjectExistsException ex) {
        if (ex.getErrors().isEmpty())
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(ex.getErrors()));
    }
    @ExceptionHandler(EmailCooldownException.class)
    public ResponseEntity<CooldownResponse> handleEmailCooldown(EmailCooldownException ex) {
        return ResponseEntity.status(429).body(new CooldownResponse(ex.getSecondsLeft()));
    }
    @ExceptionHandler(AccountLackException.class)
    public ResponseEntity<ErrorResponse> handleAccountLack() {
        return ResponseEntity.status(409).body(new ErrorResponse("Товар закончился!"));
    }
}
