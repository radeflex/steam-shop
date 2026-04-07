package by.radeflex.steamshop.http.controller.impl;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.dto.response.ConfirmationUrlResponse;
import by.radeflex.steamshop.http.controller.PaymentController;
import by.radeflex.steamshop.props.ShopProperties;
import by.radeflex.steamshop.service.payment.BalanceService;
import by.radeflex.steamshop.service.payment.OrderService;
import by.radeflex.steamshop.service.payment.PaymentService;
import by.radeflex.steamshop.utils.ValidationUtils;
import inet.ipaddr.IPAddressString;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PaymentControllerImpl implements PaymentController {
    private final PaymentService paymentService;
    private final BalanceService balanceService;
    private final OrderService orderService;
    private final ShopProperties shopProperties;

    @SneakyThrows
    private void checkRemoteAddr(String addr) {
        IPAddressString address = new IPAddressString(addr);

        boolean allowed = shopProperties.getYookassaHosts().stream()
                .map(IPAddressString::new)
                .anyMatch(range -> range.contains(address));

        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/status-webhook")
    public ResponseEntity<?> handleStatus(HttpServletRequest req,
                                          @RequestBody PaymentStatusDto dto) {
        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                .map(s -> s.split(",")[0].trim())
                .orElse(req.getRemoteAddr());
        checkRemoteAddr(ip);
        paymentService.handleNotification(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase-balance/{productId}")
    public ResponseEntity<?> purchaseViaBalance(@PathVariable Integer productId) {
        if (!orderService.purchaseViaBalance(productId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/purchase-card/{productId}")
    public ResponseEntity<ConfirmationUrlResponse> purchaseViaCard(@PathVariable Integer productId) {
        var url = orderService.purchaseViaCard(productId);
        return ResponseEntity.ok(new ConfirmationUrlResponse(url
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))));
    }

    @PostMapping("/purchase-card")
    public ResponseEntity<ConfirmationUrlResponse> purchaseCartViaCard() {
        var url = orderService.purchaseCartViaCard();
        return ResponseEntity.ok(new ConfirmationUrlResponse(url));
    }

    @PostMapping("/purchase-balance")
    public ResponseEntity<?> purchaseCartViaBalance() {
        if (!orderService.purchaseCartViaBalance()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/top-up")
    public ResponseEntity<ConfirmationUrlResponse> topUpBalance(@RequestBody @Valid TopUpDto topUpDto,
                                          BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        String url = balanceService.topUp(topUpDto);
        return ResponseEntity.ok(new ConfirmationUrlResponse(url));
    }
}
