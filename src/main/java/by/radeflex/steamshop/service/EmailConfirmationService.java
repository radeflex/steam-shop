package by.radeflex.steamshop.service;

import by.radeflex.steamshop.repository.EmailConfirmationRepository;
import by.radeflex.steamshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    private final EmailConfirmationRepository emailConfirmationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    @Transactional
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
        return userRepository.findById(AuthService.getCurrentUser().getId())
                .filter(u -> !u.getConfirmed())
                .map(u -> {
                    mailService.sendRegistration(u);
                    return true;
                }).orElse(false);
    }
}
