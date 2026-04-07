package by.radeflex.steamshop.http.controller.impl;

import by.radeflex.steamshop.dto.AccountCreateDto;
import by.radeflex.steamshop.dto.AccountReadDto;
import by.radeflex.steamshop.dto.response.CsvResponse;
import by.radeflex.steamshop.dto.response.PageResponse;
import by.radeflex.steamshop.http.controller.AccountController;
import by.radeflex.steamshop.service.payment.AccountService;
import by.radeflex.steamshop.utils.ValidationUtils;
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
public class AccountControllerImpl implements AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountReadDto> create(@RequestBody @Valid AccountCreateDto accountCreateDto,
                                                 BindingResult bindingResult) {
        ValidationUtils.checkErrors(bindingResult);
        return ResponseEntity.ok(accountService.create(accountCreateDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping
    public ResponseEntity<PageResponse<AccountReadDto>> findAll(Pageable pageable) {
        var page = PageResponse.of(accountService.findAll(pageable));
        return ResponseEntity.ok(page);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvResponse> readCsv(MultipartFile file) {
        return ResponseEntity.ok(accountService.readCsv(file));
    }
}
