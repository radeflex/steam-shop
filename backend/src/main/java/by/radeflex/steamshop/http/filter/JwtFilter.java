package by.radeflex.steamshop.http.filter;

import by.radeflex.steamshop.dto.AuthDto;
import by.radeflex.steamshop.props.JwtProperties;
import by.radeflex.steamshop.repository.UserRepository;
import by.radeflex.steamshop.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final CacheManager cacheManager;

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
            AuthDto authUser = getAuth(jwtService.getAuth(jwt).id());
            if (authUser != null && jwtService.isValid(jwt, authUser)) {
                Collection<GrantedAuthority> authorities =
                        Collections.singleton(authUser.role());
                var context = SecurityContextHolder.createEmptyContext();
                var auth = new UsernamePasswordAuthenticationToken(
                        authUser,
                        null,
                        authorities);
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        }
        filterChain.doFilter(req, resp);
    }

    private AuthDto getAuth(Integer userId) {
        var cache = cacheManager.getCache("auth");
        AuthDto auth = cache.get(userId, AuthDto.class);
        if (auth == null) {
            var user = userRepository.findById(userId).orElse(null);
            if (user == null) return null;

            auth = new AuthDto(user.getId(), user.getRole());
            cache.put(userId, auth);
        }

        return auth;
    }
}
