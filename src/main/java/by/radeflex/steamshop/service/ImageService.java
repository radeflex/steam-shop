package by.radeflex.steamshop.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.util.Optional;
import java.util.UUID;

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
            return Optional.of(s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()).readAllBytes());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }
}
