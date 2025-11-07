package by.radeflex.steamshop.exception;

public class ObjectExistsException extends RuntimeException {
    public ObjectExistsException() {
        super("уже существует");
    }
}
