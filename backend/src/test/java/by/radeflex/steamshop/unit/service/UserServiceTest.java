package by.radeflex.steamshop.unit.service;

import by.radeflex.steamshop.dto.UserCreateDto;
import by.radeflex.steamshop.dto.UserUpdateDto;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserRole;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.mapper.UserMapper;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.CurrentUserService;
import by.radeflex.steamshop.service.ImageService;
import by.radeflex.steamshop.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private final int CURRENT_USER_ID = 1;
    private final MultipartFile AVATAR = new MockMultipartFile("avatar.png", new byte[0]);
    private final String AVATAR_URL = UUID.randomUUID().toString();
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private ImageService imageService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.getCurrentUserEntity())
                .thenReturn(User.builder().id(CURRENT_USER_ID).build());
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "username"})
    void create_shouldThrow_whenFieldsExists(String value) {
        var dto = new UserCreateDto("Max", "123123", "test@gmail.com");

        if (value.equals("email")) {
            when(userRepository.findByEmail(dto.email()))
                    .thenReturn(Optional.of(new User()));
        } else {
            when(userRepository.findByUsername(dto.username()))
                    .thenReturn(Optional.of(new User()));
        }

        var e = assertThrows(ObjectExistsException.class,
                () -> userService.create(dto));
        assertTrue(e.getErrors().containsKey(value));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldWork_whenFieldsNotExists() {
        var dto = new UserCreateDto("example", "123123", "example@gmail.com");
        var encoded = "encoded";

        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(dto.password())).thenReturn(encoded);

        userService.create(dto);
        verify(userRepository).save(argThat(u ->
                u.getUsername().equals(dto.username())
                && u.getEmail().equals(dto.email())
                && u.getPassword().equals(encoded)
                && u.getRole().equals(UserRole.USER)));
        verify(userRepository).findByUsername(dto.username());
        verify(userRepository).findByEmail(dto.email());
    }

    @Test
    void update_shouldUnconfirmEmail() {
        var dto = new UserUpdateDto(null, null, "newemail@gmail.com");
        var old = User.builder()
                .id(CURRENT_USER_ID)
                .role(UserRole.USER)
                .username("example")
                .email("example@gmail.com")
                .confirmed(true).build();

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(old));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.update(dto, null);
        verify(userRepository).saveAndFlush(argThat(u ->
                u.getId().equals(CURRENT_USER_ID)
                && !u.getConfirmed()
                && u.getEmail().equals(dto.email())));
        verify(userMapper).mapFrom(old, dto);
        verify(userMapper).mapCurrentFrom(any(User.class));
    }

    @Test
    void update_shouldUpdateAvatar() {
        var dto = new UserUpdateDto(null, null, null);
        var oldUrl = UUID.randomUUID().toString();
        var old = User.builder()
                .id(CURRENT_USER_ID)
                .role(UserRole.USER)
                .avatarUrl(oldUrl).build();

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(old));
        when(imageService.upload(AVATAR)).thenReturn(AVATAR_URL);

        userService.update(dto, AVATAR);
        verify(imageService).delete(oldUrl);
        verify(imageService).upload(AVATAR);
        verify(userRepository).saveAndFlush(argThat(u ->
                u.getId().equals(CURRENT_USER_ID)
                && u.getAvatarUrl().equals(AVATAR_URL)));
    }

    @Test
    void update_shouldResetAvatar() {
        var oldUrl = UUID.randomUUID().toString();
        var old = User.builder()
                .id(CURRENT_USER_ID)
                .role(UserRole.USER)
                .avatarUrl(oldUrl).build();

        when(userRepository.findById(CURRENT_USER_ID)).thenReturn(Optional.of(old));

        userService.resetAvatar();
        verify(userRepository).findById(CURRENT_USER_ID);
        verify(imageService).delete(oldUrl);
        verify(userRepository).save(argThat(u ->
                u.getId().equals(CURRENT_USER_ID)
                && u.getAvatarUrl() == null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "username"})
    void update_shouldThrow_whenFieldsExists(String value) {
        var dto = new UserUpdateDto("Max", "123123", "test@gmail.com");

        if (value.equals("email")) {
            when(userRepository.findByEmail(dto.email()))
                    .thenReturn(Optional.of(new User()));
        } else {
            when(userRepository.findByUsername(dto.username()))
                    .thenReturn(Optional.of(new User()));
        }

        var e = assertThrows(ObjectExistsException.class, () -> userService.update(dto, AVATAR));
        assertTrue(e.getErrors().containsKey(value));
        verify(userRepository, never()).saveAndFlush(any());
        verify(imageService, never()).upload(any());
        verify(imageService, never()).delete(any());
    }
}
