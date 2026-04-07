package by.radeflex.steamshop.event.payment;

import by.radeflex.steamshop.entity.Payment;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class PaymentEvent extends ApplicationEvent {
    private final Payment payment;

    public PaymentEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }
}
