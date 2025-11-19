package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PageResponse;
import by.radeflex.steamshop.dto.UserCreateEditDto;
import by.radeflex.steamshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static by.radeflex.steamshop.validation.ValidationUtils.checkErrors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PutMapping(value = "/current", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @RequestPart("data") @Valid UserCreateEditDto userCreateEditDto,
            BindingResult bindingResult,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        checkErrors(bindingResult);
        return ResponseEntity.ok(userService.update(userCreateEditDto, image)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        return ResponseEntity.ok(userService.findCurrent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @GetMapping("/current/product-history")
    public ResponseEntity<?> getCurrentUserProductHistory(Pageable pageable) {
        var page = userService.getProductHistoryCurrent(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }
}
