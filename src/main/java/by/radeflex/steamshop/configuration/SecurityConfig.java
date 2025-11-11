package by.radeflex.steamshop.configuration;

import by.radeflex.steamshop.http.filter.JwtFilter;
import by.radeflex.steamshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProperties jwtProperties;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
        return http
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(config ->
                        config
                                .requestMatchers(HttpMethod.GET, "/products", "/products/*", "/users/current/**").permitAll()
                                .requestMatchers("/login", "/register", "/logout", "/swagger-ui/**","/v3/api-docs/**").permitAll()
//                                .requestMatchers("/products/*").hasAnyAuthority("ADMIN")
                                .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider(userService))
                .logout(config -> config.deleteCookies(jwtProperties.getCookieName()))
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(UserService userService) {
        var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userService);
        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
