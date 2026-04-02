package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.AuthDto;
import by.radeflex.steamshop.dto.response.JwtResponse;
import by.radeflex.steamshop.dto.LoginDto;
import by.radeflex.steamshop.dto.UserCreateDto;
import by.radeflex.steamshop.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    public JwtResponse register(UserCreateDto userCreateDto) {
        var user = userService.create(userCreateDto);
        var token = jwtService.generateToken(new AuthDto(user.getId(), user.getRole()));
        mailService.sendRegistration(user);
        return new JwtResponse(token);
    }

    public JwtResponse login(LoginDto loginDto) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.username(),
                        loginDto.password()));
        User pr = (User) auth.getPrincipal();
        var token = jwtService.generateToken(new AuthDto(pr.getId(), pr.getRole()));
        return new JwtResponse(token);
    }
}
