package by.radeflex.steamshop.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum PaymentStatus {
    PENDING,
    WAITING_FOR_CAPTURE,
    CANCELLED,
    SUCCEEDED;

    @JsonCreator
    public static PaymentStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(s ->
                    s.toString().equals(value.split("\\.")[1].toUpperCase()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown payment status: " + value));
    }
}
