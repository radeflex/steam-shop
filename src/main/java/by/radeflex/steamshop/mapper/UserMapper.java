package by.radeflex.steamshop.mapper;

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
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .balance(user.getBalance())
                .points(user.getPoints())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
