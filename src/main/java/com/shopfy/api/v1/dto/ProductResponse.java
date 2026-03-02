package com.shopfy.api.v1.dto;

import com.shopfy.domain.product.Product;

import java.math.BigDecimal;
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
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
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
