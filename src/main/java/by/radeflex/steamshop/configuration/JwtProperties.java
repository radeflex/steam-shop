package by.radeflex.steamshop.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtProperties {
    @Value("${jwt.cookieName}")
    private String cookieName;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationHours}")
    private int expirationHours;
}
