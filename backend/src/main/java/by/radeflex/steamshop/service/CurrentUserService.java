package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.AuthDto;
import by.radeflex.steamshop.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public User getCurrentUserEntity() {
        var auth = getAuth();
        return User.builder().id(auth.id()).role(auth.role()).build();
    }

    public Integer getCurrentUserId() {
        return getAuth().id();
    }

    private AuthDto getAuth() {
        var principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return (AuthDto) principal;
    }
}
