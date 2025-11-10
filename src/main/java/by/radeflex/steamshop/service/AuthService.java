package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.JwtResponse;
import by.radeflex.steamshop.dto.LoginDto;
import by.radeflex.steamshop.dto.UserCreateEditDto;
import by.radeflex.steamshop.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public JwtResponse register(UserCreateEditDto userCreateEditDto) {
        var user = userService.create(userCreateEditDto);
        var token = jwtService.generateToken(user);
        return new JwtResponse(token);
    }

    public JwtResponse login(LoginDto loginDto) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.username(),
                        loginDto.password()));
        var token = jwtService.generateToken((User) auth.getPrincipal());
        return new JwtResponse(token);
    }
}
