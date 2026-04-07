package by.radeflex.steamshop.event.payment;

import by.radeflex.steamshop.entity.Payment;
import lombok.Getter;

@Getter
public class ProcessOrderEvent extends PaymentEvent {
    public ProcessOrderEvent(Object source, Payment payment) {
        super(source, payment);
    }
}
