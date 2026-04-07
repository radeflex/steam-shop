package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PaymentStatusDto;
import by.radeflex.steamshop.dto.TopUpDto;
import by.radeflex.steamshop.dto.response.ConfirmationUrlResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

@Tag(name = "Payment API", description = "API для оплаты и пополнения баланса")
public interface PaymentController {
    @Hidden
    ResponseEntity<?> handleStatus(HttpServletRequest req, PaymentStatusDto dto);

    @Operation(
            summary = "Купить продукт за баланс",
            description = "Выполняет покупку одного продукта, списывая средства с баланса пользователя")
    @ApiResponse(responseCode = "200", description = "Покупка выполнена успешно")
    @ApiResponse(responseCode = "400", description = "Недостаточно средств или продукт недоступен")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    ResponseEntity<?> purchaseViaBalance(
            @Parameter(description = "ID продукта", example = "1") Integer productId);

    @Operation(
            summary = "Купить продукт картой",
            description = "Инициирует оплату картой через YooKassa, возвращает URL для перехода на страницу оплаты")
    @ApiResponse(responseCode = "200", description = "URL страницы оплаты")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    ResponseEntity<ConfirmationUrlResponse> purchaseViaCard(
            @Parameter(description = "ID продукта", example = "1") Integer productId);

    @Operation(
            summary = "Оплатить корзину картой",
            description = "Инициирует оплату всей корзины картой через YooKassa, возвращает URL для оплаты")
    @ApiResponse(responseCode = "200", description = "URL страницы оплаты")
     ResponseEntity<ConfirmationUrlResponse> purchaseCartViaCard();

    @Operation(
            summary = "Оплатить корзину за баланс",
            description = "Выполняет покупку всех товаров из корзины, списывая средства с баланса")
    @ApiResponse(responseCode = "200", description = "Корзина оплачена успешно")
    @ApiResponse(responseCode = "400", description = "Недостаточно средств или корзина пуста")
     ResponseEntity<?> purchaseCartViaBalance();

    @Operation(
            summary = "Пополнить баланс",
            description = "Инициирует пополнение баланса через YooKassa, возвращает URL для оплаты")
    @ApiResponse(responseCode = "200", description = "URL страницы пополнения")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации суммы")
     ResponseEntity<ConfirmationUrlResponse> topUpBalance(TopUpDto topUpDto,
                                                                BindingResult bindingResult);
}
