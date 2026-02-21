package by.radeflex.steamshop.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    @SneakyThrows
    public String upload(MultipartFile file) {
        String key = UUID.randomUUID().toString();

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(), RequestBody.fromBytes(file.getBytes()));

        return key;
    }

    public Optional<byte[]> get(String key) {
        try {
            if ("no-avatar".equals(key)) {
                return loadDefaultAvatar();
            }
            return loadFromS3(key);
        } catch (Exception e) {
            log.error("Failed to log image: {}", key, e);
            return Optional.empty();
        }
    }

    private @NonNull Optional<byte[]> loadFromS3(String key) throws IOException {
        try (var is = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build())) {
            return Optional.of(is.readAllBytes());
        }
    }

    private @NonNull Optional<byte[]> loadDefaultAvatar() throws IOException {
        var img = getClass().getResourceAsStream("/files/no-avatar.png");
        if (img == null) return Optional.empty();
        try (img) {
            return Optional.of(img.readAllBytes());
        }
    }

    public void delete(String key) {
        if (key.equals("no-avatar")) return;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }
}
