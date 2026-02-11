package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.service.PaymentService;
import by.radeflex.steamshop.utils.ValidationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/status-webhook")
    public ResponseEntity<?> handleStatus(@RequestBody PaymentStatusDto dto) {
        paymentService.handleNotification(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase-balance/{productId}")
    public ResponseEntity<?> purchaseViaBalance(@PathVariable Integer productId) {
        if (!paymentService.purchaseViaBalance(productId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase-card/{productId}")
    public ResponseEntity<?> purchaseCartViaCard(@PathVariable Integer productId) {
        var url = paymentService.purchaseViaCard(productId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/purchase-card")
    public ResponseEntity<?> purchaseCartViaCard() {
        var url = paymentService.purchaseCartViaCard();
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/purchase-balance")
    public ResponseEntity<?> purchaseCartViaBalance() {
        if (!paymentService.purchaseCartViaBalance()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/top-up")
    public ResponseEntity<?> topUpBalance(@RequestBody @Valid TopUpDto topUpDto,
                                          BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        String confirmationUrl = paymentService.topUp(topUpDto);
        return ResponseEntity.ok(Map.of("url", confirmationUrl));
    }
}
