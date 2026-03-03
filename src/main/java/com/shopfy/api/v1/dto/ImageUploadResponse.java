package com.shopfy.api.v1.dto;

/**
 * Response body returned after a successful image upload.
 */
public record ImageUploadResponse(Long productId, String imageUrl) {}
