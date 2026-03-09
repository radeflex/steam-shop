package by.radeflex.steamshop.exception;

import lombok.Getter;

@Getter
public class EmailCooldownException extends RuntimeException {
    private final Long secondsLeft;

    public EmailCooldownException(Long secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

}
