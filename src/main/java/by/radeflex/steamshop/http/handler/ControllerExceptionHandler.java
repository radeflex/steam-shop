package by.radeflex.steamshop.http.handler;

import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.validation.ValidationError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(ValidationError.class)
    public ResponseEntity<?> handleValidationException(ValidationError ex) {
        return ResponseEntity.badRequest().body(Map.of("errors", ex.getErrors()));
    }
    @ExceptionHandler(ObjectExistsException.class)
    public ResponseEntity<?> handleExistsException(ObjectExistsException ex) {
        return ResponseEntity.badRequest().body(Map.of("errors", ex.getErrors()));
    }
}
