package by.radeflex.steamshop.mapper;

import by.radeflex.steamshop.dto.CurrentUserReadDto;
import by.radeflex.steamshop.dto.UserInfo;
import by.radeflex.steamshop.dto.UserReadDto;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserRole;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    default User mapFrom(UserInfo dto) {
        User user = new User();
        user.setRole(UserRole.USER);
        return mapFrom(user, dto);
    }

    @BeanMapping(nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE)
    User mapFrom(@MappingTarget User old, UserInfo dto);

    UserReadDto mapFrom(User user);

    @Mapping(source = "avatarUrl", target = "avatarUrl", defaultValue = "no-image")
    CurrentUserReadDto mapCurrentFrom(User user);
}
