package by.radeflex.steamshop.service;

import by.radeflex.steamshop.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public User getCurrentUser() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User)
            return (User) principal;
        return null;
    }
}
