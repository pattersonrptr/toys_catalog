package com.toyscatalog.api.v1.dto;

import com.toyscatalog.domain.category.Category;

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
