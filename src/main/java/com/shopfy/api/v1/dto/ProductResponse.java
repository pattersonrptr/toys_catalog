package com.shopfy.api.v1.dto;

import com.shopfy.domain.product.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String brand,
        String imageUrl,
        BigDecimal price,
        int stockQuantity,
        boolean inStock,
        boolean featured,
        boolean active,
        CategoryResponse category,
        BigDecimal avgRating,
        long reviewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Convenience factory — no review data (zero-filled). Used by ProductService read paths. */
    public static ProductResponse from(Product product) {
        return from(product, 0.0, 0L);
    }

    /** Full factory — includes aggregated review data. */
    public static ProductResponse from(Product product, Double avgRating, long reviewCount) {
        BigDecimal avg = avgRating == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP);
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getImageUrl(),
                product.getPrice(),
                product.getStockQuantity(),
                product.isInStock(),
                product.isFeatured(),
                product.isActive(),
                CategoryResponse.from(product.getCategory()),
                avg,
                reviewCount,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

