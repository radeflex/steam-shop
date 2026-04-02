package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.dto.response.ConfirmationUrlResponse;
import by.radeflex.steamshop.service.payment.BalanceService;
import by.radeflex.steamshop.service.payment.OrderService;
import by.radeflex.steamshop.service.payment.PaymentService;
import by.radeflex.steamshop.utils.ValidationUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "API для оплаты и пополнения баланса")
public class PaymentController {
    private final PaymentService paymentService;
    private final BalanceService balanceService;
    private final OrderService orderService;

    @Hidden
    @PostMapping("/status-webhook")
    public ResponseEntity<?> handleStatus(@RequestBody PaymentStatusDto dto) {
        paymentService.handleNotification(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Купить продукт за баланс",
            description = "Выполняет покупку одного продукта, списывая средства с баланса пользователя")
    @ApiResponse(responseCode = "200", description = "Покупка выполнена успешно")
    @ApiResponse(responseCode = "400", description = "Недостаточно средств или продукт недоступен")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @PostMapping("/purchase-balance/{productId}")
    public ResponseEntity<?> purchaseViaBalance(
            @Parameter(description = "ID продукта", example = "1") @PathVariable Integer productId) {
        if (!orderService.purchaseViaBalance(productId)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Купить продукт картой",
            description = "Инициирует оплату картой через YooKassa, возвращает URL для перехода на страницу оплаты")
    @ApiResponse(responseCode = "200", description = "URL страницы оплаты")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @PostMapping("/purchase-card/{productId}")
    public ResponseEntity<ConfirmationUrlResponse> purchaseViaCard(
            @Parameter(description = "ID продукта", example = "1") @PathVariable Integer productId) {
        var url = orderService.purchaseViaCard(productId);
        return ResponseEntity.ok(new ConfirmationUrlResponse(url
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))));
    }

    @Operation(
            summary = "Оплатить корзину картой",
            description = "Инициирует оплату всей корзины картой через YooKassa, возвращает URL для оплаты")
    @ApiResponse(responseCode = "200", description = "URL страницы оплаты")
    @PostMapping("/purchase-card")
    public ResponseEntity<ConfirmationUrlResponse> purchaseCartViaCard() {
        var url = orderService.purchaseCartViaCard();
        return ResponseEntity.ok(new ConfirmationUrlResponse(url));
    }

    @Operation(
            summary = "Оплатить корзину за баланс",
            description = "Выполняет покупку всех товаров из корзины, списывая средства с баланса")
    @ApiResponse(responseCode = "200", description = "Корзина оплачена успешно")
    @ApiResponse(responseCode = "400", description = "Недостаточно средств или корзина пуста")
    @PostMapping("/purchase-balance")
    public ResponseEntity<?> purchaseCartViaBalance() {
        if (!orderService.purchaseCartViaBalance()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Пополнить баланс",
            description = "Инициирует пополнение баланса через YooKassa, возвращает URL для оплаты")
    @ApiResponse(responseCode = "200", description = "URL страницы пополнения")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации суммы")
    @PostMapping("/top-up")
    public ResponseEntity<ConfirmationUrlResponse> topUpBalance(@RequestBody @Valid TopUpDto topUpDto,
                                          BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        String url = balanceService.topUp(topUpDto);
        return ResponseEntity.ok(new ConfirmationUrlResponse(url));
    }
}
