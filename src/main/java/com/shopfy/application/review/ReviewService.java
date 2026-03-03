package com.shopfy.application.review;

import com.shopfy.api.v1.dto.ReviewRequest;
import com.shopfy.api.v1.dto.ReviewResponse;
import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.api.v1.exception.ResourceNotFoundException;
import com.shopfy.domain.order.OrderRepository;
import com.shopfy.domain.order.OrderStatus;
import com.shopfy.domain.product.Product;
import com.shopfy.domain.product.ProductRepository;
import com.shopfy.domain.review.Review;
import com.shopfy.domain.review.ReviewRepository;
import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;
import com.shopfy.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // ── POST /api/v1/products/{productId}/reviews ─────────────────────────────

    @Transactional
    public ReviewResponse create(Long productId, Long userId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verified-purchase check: must have at least one DELIVERED order with this product
        boolean hasPurchased = orderRepository
                .existsDeliveredOrderContainingProduct(userId, productId);
        if (!hasPurchased) {
            throw new BusinessException(
                    "You can only review products you have purchased and received");
        }

        // One review per user per product
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new BusinessException(
                    "You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        return ReviewResponse.from(reviewRepository.save(review));
    }

    // ── GET /api/v1/products/{productId}/reviews ──────────────────────────────

    public Page<ReviewResponse> findByProduct(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewResponse::from);
    }

    // ── GET /api/v1/users/me/reviews ──────────────────────────────────────────

    public Page<ReviewResponse> findByUser(Long userId, Pageable pageable) {
        return reviewRepository.findByUserId(userId, pageable)
                .map(ReviewResponse::from);
    }

    // ── DELETE /api/v1/reviews/{id} ───────────────────────────────────────────

    @Transactional
    public void delete(Long reviewId, Long requestingUserId, Role requestingRole) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        boolean isOwner = review.getUser().getId().equals(requestingUserId);
        boolean isAdmin = requestingRole == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new BusinessException("You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
    }
}
