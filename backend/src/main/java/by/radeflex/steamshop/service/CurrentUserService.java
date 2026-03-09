package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.AuthDto;
import by.radeflex.steamshop.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public User getCurrentUserEntity() {
        var auth = getAuth();
        if (auth == null) return null;
        return User.builder().id(auth.id()).role(auth.role()).build();
    }

    public Integer getCurrentUserId() {
        var auth = getAuth();
        if (auth == null) return null;
        return auth.id();
    }

    private AuthDto getAuth() {
        var principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof AuthDto)
            return (AuthDto) principal;
        return null;
    }
}
