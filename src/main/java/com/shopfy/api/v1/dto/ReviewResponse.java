package com.shopfy.api.v1.dto;

import com.shopfy.domain.review.Review;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        String productName,
        Long userId,
        String userName,
        int rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
