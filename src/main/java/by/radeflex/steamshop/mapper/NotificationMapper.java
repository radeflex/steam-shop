package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.entity.Notification;
import by.radeflex.steamshop.entity.NotificationType;
import by.radeflex.steamshop.entity.Payment;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.service.AuthService;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    private Notification buildPayment(String text, Payment payment) {
        return Notification.builder()
                .user(payment.getUser())
                .title("Заказ №" + payment.getOrderId())
                .text(text)
                .paymentStatus(payment.getStatus())
                .payment(payment)
                .type(NotificationType.PAYMENT)
                .build();
    }

    public NotificationReadDto mapFrom(Notification notification) {
        return NotificationReadDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .text(notification.getText())
                .type(notification.getType().name())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public Notification mapFrom(Notification n, Payment payment) {
        var url = payment.getConfirmationUrl();
        var text = switch (payment.getStatus()) {
            case CANCELLED -> "Оплата отменена.";
            case WAITING_FOR_CAPTURE, PENDING -> url;
            case SUCCEEDED -> "Успешно завершен. Проверьте email.";
        };
        if (n == null)
            return buildPayment(text, payment);
        else {
            n.setPaymentStatus(payment.getStatus());
            n.setText(text);
            return n;
        }
    }

    public Notification mapFrom(NotificationCreateDto dto, User user) {
        return Notification.builder()
                .title(dto.title())
                .text(dto.text())
                .type(NotificationType.INFO)
                .createdBy(AuthService.getCurrentUser())
                .user(user)
                .build();
    }
}
