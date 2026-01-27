package by.radeflex.steamshop.service;

import by.radeflex.steamshop.configuration.MailProperties;
import by.radeflex.steamshop.entity.EmailConfirmation;
import by.radeflex.steamshop.entity.User;
import by.radeflex.steamshop.repository.EmailConfirmationRepository;
import freemarker.template.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final Configuration freemarkerConfig;
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final MailProperties mailProperties;

    @SneakyThrows
    private String getRegisterHtml(User user) {
        var ec = emailConfirmationRepository.findByUserId(user.getId());
        var writer = new StringWriter();
        var token = ec.isEmpty() ? UUID.randomUUID() : ec.get().getToken();
        Map<String, Object> object = new HashMap<>();

        object.put("username", user.getUsername());
        object.put("token", token);
        if (ec.isEmpty())
            emailConfirmationRepository.save(EmailConfirmation.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now()
                        .plusDays(mailProperties.getExpirationDays()))
                .build());
        freemarkerConfig.getTemplate("email-confirm.ftlh")
                .process(object, writer);
        return writer.getBuffer().toString();
    }

    @SneakyThrows
    public void sendRegistration(User user) {
        var html = getRegisterHtml(user);
        var message = javaMailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(user.getEmail());
        helper.setSubject("steamshop812: Подтверждение Email");
        helper.setText(html, true);
        helper.setFrom(mailProperties.getUsername());
        javaMailSender.send(message);
    }
}
