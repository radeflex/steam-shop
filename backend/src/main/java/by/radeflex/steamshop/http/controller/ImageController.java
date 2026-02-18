package by.radeflex.steamshop.http.controller;

import by.radeflex.steamshop.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/files")
public class ImageController {
    private final ImageService imageService;

    @GetMapping(value = "/{uuid}", produces = {"image/png", "image/jpg"})
    public ResponseEntity<byte[]> get(@PathVariable String uuid) {
        return ResponseEntity.ok(imageService.get(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }
}
