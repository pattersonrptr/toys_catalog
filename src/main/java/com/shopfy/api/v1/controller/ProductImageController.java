package com.shopfy.api.v1.controller;

import com.shopfy.api.v1.dto.ImageUploadResponse;
import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.application.product.ProductService;
import com.shopfy.infrastructure.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Handles product image upload and removal.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST   /api/v1/products/{id}/image} — upload (replace) product image</li>
 *   <li>{@code DELETE /api/v1/products/{id}/image} — remove product image</li>
 * </ul>
 *
 * <p>Both endpoints require {@code ROLE_ADMIN}.
 */
@RestController
@RequestMapping("/api/v1/products/{id}/image")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Images", description = "Upload and remove product images (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class ProductImageController {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Tika TIKA = new Tika();

    private final ProductService productService;
    private final StorageService storageService;

    // -------------------------------------------------------------------------
    // POST /api/v1/products/{id}/image
    // -------------------------------------------------------------------------

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Upload product image",
            description = "Uploads (or replaces) the image for a product. " +
                    "Accepted types: JPEG, PNG, WebP. Max size: 5 MB.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Image uploaded",
                            content = @Content(schema = @Schema(implementation = ImageUploadResponse.class))),
                    @ApiResponse(responseCode = "400", description = "File too large or missing"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "403", description = "Not ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Product not found"),
                    @ApiResponse(responseCode = "415", description = "Unsupported image type")
            }
    )
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) throws IOException {

        validateFile(file);

        byte[] bytes = file.getBytes();
        String detectedMime = TIKA.detect(bytes);
        if (!ALLOWED_MIME_TYPES.contains(detectedMime)) {
            throw new BusinessException(
                    "Unsupported image type: " + detectedMime +
                    ". Allowed types: image/jpeg, image/png, image/webp");
        }

        String extension = extensionFor(detectedMime);
        String key = "products/" + id + "/" + UUID.randomUUID() + "." + extension;

        // If product already has an image, delete it from storage first
        String previousUrl = productService.updateImageUrl(id, "PENDING");
        if (previousUrl != null && !previousUrl.isEmpty()) {
            String previousKey = extractKey(previousUrl);
            if (previousKey != null) {
                try {
                    storageService.delete(previousKey);
                } catch (Exception e) {
                    log.warn("Could not delete previous image key={}: {}", previousKey, e.getMessage());
                }
            }
        }

        String imageUrl = storageService.upload(key, file.getInputStream(), bytes.length, detectedMime);
        productService.updateImageUrl(id, imageUrl);

        log.info("Image uploaded for product id={} key={}", id, key);
        return ResponseEntity.ok(new ImageUploadResponse(id, imageUrl));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/products/{id}/image
    // -------------------------------------------------------------------------

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Remove product image",
            description = "Deletes the stored image and clears the image URL on the product.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Image removed"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "403", description = "Not ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        String previousUrl = productService.removeImageUrl(id);
        if (previousUrl != null && !previousUrl.isEmpty()) {
            String key = extractKey(previousUrl);
            if (key != null) {
                try {
                    storageService.delete(key);
                } catch (Exception e) {
                    log.warn("Could not delete image key={}: {}", key, e.getMessage());
                }
            }
        }
        log.info("Image removed for product id={}", id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No file provided or file is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException("File exceeds maximum size of 5 MB");
        }
    }

    private static String extensionFor(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default           -> "bin";
        };
    }

    /**
     * Extracts the object key from a full public URL.
     * URL pattern: {@code <publicUrl>/<bucket>/<key>}
     * The key itself may contain slashes (e.g. {@code products/42/uuid.jpg}).
     *
     * <p>Returns {@code null} if the URL doesn't match the expected pattern.
     */
    private String extractKey(String url) {
        // The key starts after the third "/" counting from the scheme
        // e.g. http://minio:9000/shopfy-images/products/42/abc.jpg
        //                        ^^^^^^^^^^^^^ = bucket (skip), rest is key
        try {
            int schemeEnd = url.indexOf("://");
            if (schemeEnd < 0) return null;
            int firstSlash = url.indexOf('/', schemeEnd + 3);   // after host:port
            if (firstSlash < 0) return null;
            int secondSlash = url.indexOf('/', firstSlash + 1); // after bucket
            if (secondSlash < 0) return null;
            return url.substring(secondSlash + 1);
        } catch (Exception e) {
            log.warn("Could not parse storage key from URL: {}", url);
            return null;
        }
    }
}
