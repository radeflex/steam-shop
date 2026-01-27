package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CurrentUserReadDto;
import by.radeflex.steamshop.dto.UserInfo;
import by.radeflex.steamshop.dto.UserReadDto;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    private void buildUser(User user, UserInfo dto) {
        if (dto.username() != null) user.setUsername(dto.username());
        if (dto.password() != null) user.setPassword(dto.password());
        if (dto.email() != null) user.setEmail(dto.email());
    }

    public User mapFrom(UserInfo dto) {
        User user = new User();
        buildUser(user, dto);
        user.setRole(UserRole.USER);
        user.setAvatarUrl("no-avatar");
        return user;
    }

    public User mapFrom(User user, UserInfo dto) {
        buildUser(user, dto);
        return user;
    }

    public UserReadDto mapFrom(User user) {
        return UserReadDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .avatarUrl(user.getAvatarUrl())
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
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
