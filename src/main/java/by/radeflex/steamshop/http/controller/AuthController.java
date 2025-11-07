package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.configuration.JwtProperties;
import by.radeflex.steamshop.dto.LoginUserDto;
import by.radeflex.steamshop.dto.UserCreateEditDto;
import by.radeflex.steamshop.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtProperties jwtProperties;
    private final AuthService authService;

    private void addCookie(String token, HttpServletResponse response) {
        var cookie = new Cookie(jwtProperties.getCookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtProperties.getExpirationHours() * 3600);
        response.addCookie(cookie);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletResponse resp,
                                      @RequestBody UserCreateEditDto userCreateEditDto) {
        var jwtResponse = authService.register(userCreateEditDto);
        addCookie(jwtResponse.token(), resp);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletResponse resp,
                                   @RequestBody LoginUserDto loginUserDto) {
        var jwtResponse = authService.login(loginUserDto);
        addCookie(jwtResponse.token(), resp);
        return ResponseEntity.ok(jwtResponse);
    }
}
