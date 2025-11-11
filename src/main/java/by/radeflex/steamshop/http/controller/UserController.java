package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.dto.UserCreateEditDto;
import by.radeflex.steamshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PutMapping("/current")
    public ResponseEntity<?> update(
            @RequestBody @Valid UserCreateEditDto userCreateEditDto) {
        return ResponseEntity.ok(userService.update(userCreateEditDto)
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
    public ResponseEntity<?> getCurrentUserProductHistory() {
        return ResponseEntity.ok(userService.getProductHistoryCurrent());
    }
}
