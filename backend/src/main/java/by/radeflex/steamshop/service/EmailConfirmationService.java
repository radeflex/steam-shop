package by.radeflex.steamshop.service;

import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.exception.EmailCooldownException;
import by.radeflex.steamshop.props.MailProperties;
import by.radeflex.steamshop.repository.EmailConfirmationRepository;
import by.radeflex.steamshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final MailProperties mailProperties;
    private final CurrentUserService currentUserService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    @CacheEvict(
            value = "user::current",
            key = "@currentUserService.getCurrentUserId()",
            condition = "#result")
    public boolean confirmEmail(UUID token) {
        return emailConfirmationRepository.findByToken(token)
                .map(ec -> {
                    var u = ec.getUser();
                    u.setConfirmed(true);
                    userRepository.saveAndFlush(u);
                    emailConfirmationRepository.delete(ec);
                    return true;
                }).orElse(false);
    }

    public boolean sendEmailConfirmation() {
        return userRepository.findById(currentUserService.getCurrentUserEntity().getId())
                .filter(u -> !u.getConfirmed())
                .map(u -> {
                    checkCooldown(u);
                    mailService.sendRegistration(u);
                    return true;
                }).orElse(false);
    }

    private void checkCooldown(User u) {
        var key = "email_cd::" + u.getId();
        Duration cooldown = Duration.ofSeconds(mailProperties.getCooldownSeconds());
        Boolean setted = redisTemplate.opsForValue().setIfAbsent(key, "1", cooldown);
        if (Boolean.FALSE.equals(setted))
            throw new EmailCooldownException(redisTemplate.getExpire(key));
    }
}
