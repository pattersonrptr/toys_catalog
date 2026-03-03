package com.shopfy.infrastructure.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

/**
 * {@link StorageService} backed by an S3-compatible MinIO instance via AWS SDK v2.
 *
 * <p>Public URL pattern: {@code <publicUrl>/<bucket>/<key>}
 * (MinIO path-style access with anonymous download policy on the bucket).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements StorageService {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public String upload(String key, InputStream data, long size, String contentType) {
        var request = PutObjectRequest.builder()
                .bucket(storageProperties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(data, size));

        String url = buildPublicUrl(key);
        log.info("Uploaded object: bucket={} key={} url={}", storageProperties.bucket(), key, url);
        return url;
    }

    @Override
    public void delete(String key) {
        var request = DeleteObjectRequest.builder()
                .bucket(storageProperties.bucket())
                .key(key)
                .build();

        s3Client.deleteObject(request);
        log.info("Deleted object: bucket={} key={}", storageProperties.bucket(), key);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String buildPublicUrl(String key) {
        String base = storageProperties.publicUrl();
        // Trim trailing slash so we always produce exactly one slash between segments
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/" + storageProperties.bucket() + "/" + key;
    }
}
