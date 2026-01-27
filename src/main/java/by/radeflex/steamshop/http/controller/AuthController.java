package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.configuration.JwtProperties;
import by.radeflex.steamshop.dto.LoginDto;
import by.radeflex.steamshop.dto.UserCreateDto;
import by.radeflex.steamshop.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static by.radeflex.steamshop.validation.ValidationUtils.checkErrors;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final JwtProperties jwtProperties;
    private final AuthService authService;

    private void addCookie(String token, HttpServletResponse response) {
        var cookie = new Cookie(jwtProperties.getCookieName(), token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtProperties.getExpirationDays() * 24 * 3600);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletResponse resp,
                                      @RequestBody @Valid UserCreateDto userCreateDto,
                                      BindingResult bindingResult) {
        checkErrors(bindingResult);
        var jwtResponse = authService.register(userCreateDto);
        addCookie(jwtResponse.token(), resp);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletResponse resp,
                                   @RequestBody LoginDto loginDto) {
        var jwtResponse = authService.login(loginDto);
        addCookie(jwtResponse.token(), resp);
        return ResponseEntity.ok(jwtResponse);
    }
}
