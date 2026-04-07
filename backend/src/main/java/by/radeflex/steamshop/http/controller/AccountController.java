package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.dto.response.CsvResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Account API",
        description = "API для взаимодействия с аккаунтами")
public interface AccountController {
    @Operation(
            summary = "Добавить Steam аккаунт в базу доступных",
            description = "Возвращает созданный аккаунт")
    @ApiResponse(responseCode = "200", description = "Аккаунт создан")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @ApiResponse(responseCode = "400", description = "Ошибка в заполнении данных")
    ResponseEntity<AccountReadDto> create(AccountCreateDto accountCreateDto,
                                                 BindingResult bindingResult);

    @Operation(
            summary = "Найти все аккаунты",
            description = "Возвращает страницу всех аккаунтов")
    ResponseEntity<PageResponse<AccountReadDto>> findAll(Pageable pageable);

    @Operation(
            summary = "Импортировать аккаунты из CSV",
            description = "Возвращает объект результатов импорта"
    )
    @ApiResponse(responseCode = "200", description = "Аккаунты добавлены")
    ResponseEntity<CsvResponse> readCsv(MultipartFile file);
}
