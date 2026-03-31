package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.NotificationCreateDto;
import by.radeflex.steamshop.entity.Notification;
import by.radeflex.steamshop.entity.NotificationRead;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.mapper.NotificationMapper;
import by.radeflex.steamshop.repository.NotificationReadRepository;
import by.radeflex.steamshop.repository.NotificationRepository;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {
    private final int CURRENT_USER_ID = 2;
    private final int NOTIFICATION_ID = 4;
    private final int USER_ID = 1;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationReadRepository notificationReadRepository;
    @Mock
    private CurrentUserService currentUserService;
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentUserEntity())
                .thenReturn(User.builder().id(CURRENT_USER_ID).build());

        notificationMapper = spy(new NotificationMapper(currentUserService));
        notificationService = new NotificationService(
                userRepository,
                notificationRepository,
                notificationReadRepository,
                notificationMapper,
                currentUserService
        );
    }

    @Test
    void sendToUser_shouldReturnDto_whenUserExists() {
        var dto = new NotificationCreateDto("example", "text");
        var cur = currentUserService.getCurrentUserEntity();
        var user = User.builder().id(USER_ID).build();

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = notificationService.sendToUser(USER_ID, dto);
        assertTrue(result.isPresent());
        verify(userRepository).findById(USER_ID);
        verify(notificationMapper).mapFrom(dto, user);
        verify(notificationRepository).save(argThat(n ->
                n.getCreatedBy().equals(cur)
            && n.getUser().getId().equals(USER_ID)
            && n.getTitle().equals(dto.title())
            && n.getText().equals(dto.text())));
        verify(notificationMapper).mapFrom(any(Notification.class));
    }

    @Test
    void sendToUser_shouldReturnEmpty_whenUserNotExists() {
        var dto = new NotificationCreateDto("example", "text");

        when(userRepository.findById(USER_ID))
                .thenReturn(Optional.empty());

        var result = notificationService.sendToUser(USER_ID, dto);
        assertTrue(result.isEmpty());
        verify(userRepository).findById(USER_ID);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void read_shouldReturnTrue_whenNotificationIsUnread() {
        var u = currentUserService.getCurrentUserEntity();
        var n = Notification.builder()
                .id(NOTIFICATION_ID)
                .user(u)
                .build();
        when(notificationRepository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(n));
        when(notificationReadRepository.existsByNotificationAndUser(eq(n), eq(u)))
                .thenReturn(false);

        var result = notificationService.read(NOTIFICATION_ID);
        assertTrue(result);
        verify(notificationRepository).findById(NOTIFICATION_ID);
        verify(notificationReadRepository).existsByNotificationAndUser(n, u);
        verify(notificationReadRepository).save(argThat(nr ->
                nr.getNotification().getId().equals(n.getId())
                && nr.getUser().equals(u)));
    }

    @Test
    void read_shouldReturnFalse_whenNotificationNotExists() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.empty());

        var result = notificationService.read(NOTIFICATION_ID);
        assertFalse(result);
        verify(notificationRepository).findById(NOTIFICATION_ID);
        verify(notificationReadRepository, never()).save(any(NotificationRead.class));
    }

    @Test
    void read_shouldReturnFalse_whenNotificationIsRead() {
        var u = currentUserService.getCurrentUserEntity();
        var n = Notification.builder()
                .id(NOTIFICATION_ID)
                .user(u)
                .build();

        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(n));
        when(notificationReadRepository.existsByNotificationAndUser(eq(n), eq(u)))
                .thenReturn(true);

        var result = notificationService.read(NOTIFICATION_ID);
        assertFalse(result);
        verify(notificationRepository).findById(NOTIFICATION_ID);
        verify(notificationReadRepository).existsByNotificationAndUser(n, u);
        verify(notificationReadRepository, never()).save(any(NotificationRead.class));
    }

    @Test
    void delete_shouldReturnTrue_whenNotificationExists() {
        var u = currentUserService.getCurrentUserEntity();
        var n = Notification.builder()
                .id(NOTIFICATION_ID)
                .user(u).build();

        when(notificationRepository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(n));

        var result = notificationService.delete(NOTIFICATION_ID);
        assertTrue(result);
        verify(notificationRepository).findById(NOTIFICATION_ID);
        verify(notificationRepository).delete(argThat(nf ->
                nf.getId().equals(NOTIFICATION_ID)));
    }

    @Test
    void delete_shouldReturnFalse_whenNotificationNotExists() {
        when(notificationRepository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.empty());

        var result = notificationService.delete(NOTIFICATION_ID);
        assertFalse(result);
        verify(notificationRepository).findById(NOTIFICATION_ID);
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    @Test
    void delete_shouldThrow_whenCurrentUserIsNotOwner() {
        var n = Notification.builder()
                .id(NOTIFICATION_ID)
                .user(User.builder().id(322).build())
                .build();

        when(notificationRepository.findById(NOTIFICATION_ID))
                .thenReturn(Optional.of(n));
        assertThrows(IllegalStateException.class, () -> notificationService.delete(NOTIFICATION_ID));
        verify(notificationRepository, never()).delete(any(Notification.class));
    }
}
