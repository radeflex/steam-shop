package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.dto.NotificationReadDto;
import by.radeflex.steamshop.entity.Notification;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.service.AuthService;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    public NotificationReadDto mapFrom(Notification notification) {
        return NotificationReadDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .text(notification.getText())
                .type(notification.getType().name())
                .createdAt(notification.getCreatedAt())
                .build();
    }
    public Notification mapFrom(NotificationCreateDto dto, User user) {
        return Notification.builder()
                .title(dto.title())
                .text(dto.text())
                .type(dto.type())
                .createdBy(AuthService.getCurrentUser())
                .user(user)
                .build();
    }
}
