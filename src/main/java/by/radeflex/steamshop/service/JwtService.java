package by.radeflex.steamshop.service;

import by.radeflex.steamshop.configuration.JwtProperties;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    public boolean isValid(String token, User u) {
        return !isExpired(token) && getUser(token).equals(u);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(genSecretKey())
                .build()
                .parseClaimsJws(token).getBody();
    }

    public User getUser(String token) {
        var claims = extractClaims(token);
        return User.builder()
                .id(claims.get("id", Integer.class))
                .role(UserRole.valueOf(claims.get("role", String.class)))
                .build();
    }

    public String generateToken(User user) {
        var days = jwtProperties.getExpirationDays();
        return Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + days * 24 * 3600 * 1000L))
                .claim("id", user.getId())
                .claim("role", user.getRole())
                .signWith(genSecretKey())
                .compact();
    }

    private SecretKey genSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
