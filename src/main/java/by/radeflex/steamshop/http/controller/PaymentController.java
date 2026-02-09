package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.PurchaseCreateDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.service.PaymentService;
import by.radeflex.steamshop.validation.ValidationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseCreateDto cartItems) {
        var url = paymentService.purchaseViaCard(cartItems);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/purchase-balance")
    public ResponseEntity<?> purchaseViaBalance(@RequestBody PurchaseCreateDto cartItems) {
        if (!paymentService.purchaseViaBalance(cartItems)) {
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
