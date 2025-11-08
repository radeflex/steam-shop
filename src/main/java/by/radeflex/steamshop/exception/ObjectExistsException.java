package by.radeflex.steamshop.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ObjectExistsException extends RuntimeException {
    private final Map<String, String> errors = new HashMap<>();
    public ObjectExistsException(List<String> fields) {
        super("Such object already exists");
        for (String field : fields) {
            errors.put(field, "уже существует");
        }
    }
}
