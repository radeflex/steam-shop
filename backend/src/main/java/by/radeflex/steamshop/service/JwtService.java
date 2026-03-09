package by.radeflex.steamshop.service;

import by.radeflex.steamshop.dto.AuthDto;
import by.radeflex.steamshop.entity.UserRole;
import by.radeflex.steamshop.props.JwtProperties;
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

    public boolean isValid(String token, AuthDto dto) {
        return !isExpired(token) && getAuth(token).equals(dto);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(genSecretKey())
                .build()
                .parseClaimsJws(token).getBody();
    }

    public AuthDto getAuth(String token) {
        var claims = extractClaims(token);
        return new AuthDto(
                claims.get("id", Integer.class),
                UserRole.valueOf(claims.get("role", String.class)));
    }

    public String generateToken(AuthDto dto) {
        var days = jwtProperties.getExpirationDays();
        return Jwts.builder()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + days * 24 * 3600 * 1000L))
                .claim("id", dto.id())
                .claim("role", dto.role().getAuthority())
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
