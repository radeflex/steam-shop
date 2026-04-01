package by.radeflex.steamshop.dto;

import by.radeflex.steamshop.entity.PaymentStatus;
import java.util.UUID;

public record PaymentStatusDto(
        PaymentObject object,
        PaymentStatus event,
        String url
) {
    public PaymentStatusDto(UUID id, PaymentStatus event, String url) {
        this(new PaymentObject(id), event, url);
    }
    public UUID id() {return object.id();}
}

record PaymentObject(
        UUID id
) {}