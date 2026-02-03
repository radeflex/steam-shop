package by.radeflex.steamshop.exception;

public class AccountLackException extends RuntimeException {
    public AccountLackException() {
        super("lack of accounts in the storage for this amount");
    }
}
