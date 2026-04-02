package by.radeflex.steamshop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с URL для перехода к оплате или подтверждению")
public record ConfirmationUrlResponse(
        @Schema(description = "URL для перехода", example = "https://yookassa.ru/checkout/payments/abc123")
        String url
) {
}
