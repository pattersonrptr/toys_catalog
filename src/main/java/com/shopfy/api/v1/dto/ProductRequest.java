package com.shopfy.api.v1.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
        String name,

        String description,

        @NotBlank(message = "Brand is required")
        @Size(min = 1, max = 100)
        String brand,

        @Size(max = 500)
        String imageUrl,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal digits")
        BigDecimal price,

        @Min(value = 0, message = "Stock quantity cannot be negative")
        int stockQuantity,

        boolean featured,

        @NotNull(message = "Category ID is required")
        Long categoryId
) {}
