package com.shopfy.api.v1.dto;

import com.shopfy.domain.category.Category;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive()
        );
    }
}
