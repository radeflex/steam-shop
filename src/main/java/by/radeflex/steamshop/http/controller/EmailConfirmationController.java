package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.service.EmailConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EmailConfirmationController {
    private final EmailConfirmationService emailConfirmationService;

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam UUID token) {
        if (!emailConfirmationService.confirmEmail(token))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-email-confirmation")
    public ResponseEntity<?> sendEmailConfirmation() {
        if (!emailConfirmationService.sendEmailConfirmation())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return ResponseEntity.ok().build();
    }
}
