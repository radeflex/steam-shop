package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.CurrentUserReadDto;
import by.radeflex.steamshop.dto.UserCreateDto;
import by.radeflex.steamshop.dto.UserUpdateDto;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserRole;
import by.radeflex.steamshop.exception.ObjectExistsException;
import by.radeflex.steamshop.mapper.UserMapper;
import by.radeflex.steamshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private final MultipartFile AVATAR = new MockMultipartFile("avatar.png", new byte[0]);
    private final String AVATAR_URL = UUID.randomUUID().toString();
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;
    @Mock
    private ImageService imageService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(
                passwordEncoder,
                userRepository,
                new UserMapper(),
                null,
                null,
                imageService,
                authService
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "username"})
    void isExistingNotCreated(String value) {
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
    void checkCreateUser() {
        var userDto = new UserCreateDto("Max", "123123", "test@gmail.com");
        var encoded = "encoded";
        var user = User.builder()
                .username(userDto.username())
                .password(encoded)
                .email(userDto.email())
                .role(UserRole.USER)
                .avatarUrl("no-avatar")
                .build();

        when(userRepository.save(user)).thenReturn(user);
        when(passwordEncoder.encode(userDto.password())).thenReturn(encoded);

        var result = userService.create(userDto);
        verify(userRepository).save(user);
        verify(userRepository).findByUsername(userDto.username());
        verify(userRepository).findByEmail(userDto.email());
        assertEquals(user, result);
    }

    @Test
    void checkUpdateUser() {
        var nowDto = new UserUpdateDto("maxik", null, "test12@gmail.com");
        final String newAvatarUrl = UUID.randomUUID().toString();
        final Integer CURRENT_ID = 1;
        var old = User.builder()
                .id(CURRENT_ID)
                .username("Max")
                .password("123123")
                .email("test@gmail.com")
                .avatarUrl(AVATAR_URL)
                .role(UserRole.USER)
                .build();
        var now = User.builder()
                .id(CURRENT_ID)
                .username(nowDto.username())
                .password(old.getPassword())
                .email(nowDto.email())
                .avatarUrl(newAvatarUrl)
                .role(UserRole.USER)
                .build();
        var nowReadDto = CurrentUserReadDto.builder()
                .id(CURRENT_ID)
                .username(now.getUsername())
                .email(now.getEmail())
                .avatarUrl(now.getAvatarUrl())
                .confirmed(old.getConfirmed())
                .role(UserRole.USER.toString())
                .build();

        when(authService.getCurrentUser()).thenReturn(old);
        when(userRepository.saveAndFlush(now)).thenReturn(now);
        when(userRepository.findById(CURRENT_ID)).thenReturn(Optional.of(old));
        when(imageService.upload(AVATAR)).thenReturn(newAvatarUrl);

        var result = userService.update(nowDto, AVATAR);
        assertTrue(result.isPresent());
        assertEquals(nowReadDto, result.get());
        assertFalse(old.getConfirmed());

        verify(imageService).delete(AVATAR_URL);
        verify(imageService).upload(AVATAR);
        verify(userRepository).saveAndFlush(now);
        verify(userRepository).findByUsername(nowDto.username());
        verify(userRepository).findByEmail(nowDto.email());
    }
}
