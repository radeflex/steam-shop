package by.radeflex.steamshop.http.filter;

import by.radeflex.steamshop.configuration.JwtProperties;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws ServletException, IOException {
        var cookies = req.getCookies();
        if (cookies == null) {
            filterChain.doFilter(req, resp);
            return;
        }
        var jwt = Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(jwtProperties.getCookieName()))
                .findFirst().map(Cookie::getValue).orElse(null);
        if (jwt == null) {
            filterChain.doFilter(req, resp);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            var user = userRepository.findById(jwtService.getUser(jwt).getId())
                    .orElse(null);
            if (user != null && jwtService.isValid(jwt, user)) {
                var context = SecurityContextHolder.createEmptyContext();
                var auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities());
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(req, resp);
    }
}
