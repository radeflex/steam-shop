package by.radeflex.steamshop.validation;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationError extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationError(Map<String, String> errors) {
        super("Validation error");
        this.errors = errors;
    }

}
