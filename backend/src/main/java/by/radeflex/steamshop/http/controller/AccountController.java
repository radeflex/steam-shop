package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.dto.response.CsvResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.service.payment.AccountService;
import by.radeflex.steamshop.utils.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@PreAuthorize("hasAuthority('ADMIN')")
@Tag(name = "Account API",
        description = "API для взаимодействия с аккаунтами")
public class AccountController {
    private final AccountService accountService;

    @Operation(
            summary = "Добавить Steam аккаунт в базу доступных",
            description = "Возвращает созданный аккаунт")
    @ApiResponse(responseCode = "200", description = "Аккаунт создан")
    @ApiResponse(responseCode = "404", description = "Продукт не найден")
    @ApiResponse(responseCode = "400", description = "Ошибка в заполнении данных")
    @PostMapping
    public ResponseEntity<AccountReadDto> create(@RequestBody @Valid AccountCreateDto accountCreateDto,
                                                 BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(accountService.create(accountCreateDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @Operation(
            summary = "Найти все аккаунты",
            description = "Возвращает страницу всех аккаунтов")
    @GetMapping
    public ResponseEntity<PageResponse<AccountReadDto>> findAll(Pageable pageable) {
        var page = PageResponse.of(accountService.findAll(pageable));
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Импортировать аккаунты из CSV",
            description = "Возвращает объект результатов импорта"
    )
    @ApiResponse(responseCode = "200", description = "Аккаунты добавлены")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvResponse> readCsv(MultipartFile file) {
        return ResponseEntity.ok(accountService.readCsv(file));
    }
}
