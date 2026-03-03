package com.shopfy.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the object-storage backend (MinIO / S3-compatible).
 * Bound from the {@code shopfy.storage.*} block in application.yml.
 */
@ConfigurationProperties(prefix = "shopfy.storage")
public record StorageProperties(
        String endpoint,
        String bucket,
        String accessKey,
        String secretKey,
        String publicUrl
) {}
