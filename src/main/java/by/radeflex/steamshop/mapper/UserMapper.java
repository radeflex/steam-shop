package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CurrentUserReadDto;
import by.radeflex.steamshop.dto.UserCreateEditDto;
import by.radeflex.steamshop.dto.UserReadDto;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private void buildUser(User user, UserCreateEditDto dto) {
        user.setUsername(dto.username());
        user.setPassword(dto.password());
        user.setEmail(dto.email());
        user.setRole(UserRole.USER);
    }

    public User mapFrom(UserCreateEditDto userCreateEditDto) {
        User user = new User();
        buildUser(user, userCreateEditDto);
        return user;
    }

    public User mapFrom(User user, UserCreateEditDto userCreateEditDto) {
        buildUser(user, userCreateEditDto);
        return user;
    }

    public UserReadDto mapFrom(User user) {
        return UserReadDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public CurrentUserReadDto mapCurrentFrom(User user) {
        return CurrentUserReadDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .balance(user.getBalance())
                .points(user.getPoints())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .build();
    }
}
