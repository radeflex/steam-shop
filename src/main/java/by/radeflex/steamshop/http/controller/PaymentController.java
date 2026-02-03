package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.PurchaseCreateDto;
import by.radeflex.steamshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseCreateDto cartItems) {
        var url = paymentService.purchase(cartItems);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
