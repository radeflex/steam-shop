package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.PageResponse;
import by.radeflex.steamshop.dto.UserUpdateDto;
import by.radeflex.steamshop.filter.UserFilter;
import by.radeflex.steamshop.service.UserService;
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

import static by.radeflex.steamshop.utils.ValidationUtils.checkErrors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<?> findAll(UserFilter filter, Pageable pageable) {
        var page = PageResponse.of(userService.findAll(filter, pageable));
        return ResponseEntity.ok(page);
    }

    @PutMapping(value = "/current", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @RequestPart(value = "data", required = false) @Valid UserUpdateDto userUpdateDto,
            BindingResult bindingResult,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        checkErrors(bindingResult);
        return ResponseEntity.ok(userService.update(userUpdateDto, image)
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

    @DeleteMapping("/current/avatar")
    public ResponseEntity<?> deleteCurrentUserAvatar() {
        userService.resetAvatar();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current/product-history")
    public ResponseEntity<?> getCurrentUserProductHistory(Pageable pageable) {
        var page = userService.getProductHistoryCurrent(pageable);
        return ResponseEntity.ok(PageResponse.of(page));
    }
}
