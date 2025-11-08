package by.radeflex.steamshop.validation;

import lombok.experimental.UtilityClass;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.stream.Collectors;

@UtilityClass
public class ValidationUtils {
    public static void checkErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            var errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage));
            throw new ValidationError(errors);
        }
    }
}
