package by.radeflex.steamshop.event.payment;

import by.radeflex.steamshop.entity.Payment;
import lombok.Getter;

@Getter
public class CreateOrderEvent extends PaymentEvent {
    public CreateOrderEvent(Object source, Payment payment) {
        super(source, payment);
    }
}
