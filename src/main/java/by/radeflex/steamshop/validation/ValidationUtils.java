package by.radeflex.steamshop.validation;

import lombok.experimental.UtilityClass;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ValidationUtils {
    public static Map<String, String> mapFieldErrors(List<FieldError> errors) {
        return errors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage));
    }
}
