package com.shopfy.infrastructure.storage;

import java.io.InputStream;

/**
 * Port for object storage operations.
 * The primary adapter is {@link MinioStorageService} (S3-compatible via AWS SDK v2).
 */
public interface StorageService {

    /**
     * Uploads an object and returns its public-accessible URL.
     *
     * @param key         the storage key (path inside the bucket), e.g. {@code products/42/uuid.jpg}
     * @param data        the raw bytes to upload
     * @param size        exact byte length of {@code data}
     * @param contentType MIME type, e.g. {@code image/jpeg}
     * @return the public URL to retrieve the object
     */
    String upload(String key, InputStream data, long size, String contentType);

    /**
     * Deletes an object. No-op if the key does not exist.
     *
     * @param key the storage key used when uploading
     */
    void delete(String key);
}
