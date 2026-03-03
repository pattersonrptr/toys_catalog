package com.shopfy.api.v1.controller;

import com.shopfy.api.v1.dto.ReviewRequest;
import com.shopfy.api.v1.dto.ReviewResponse;
import com.shopfy.application.review.ReviewService;
import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Product reviews and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    // ── POST /api/v1/products/{productId}/reviews ─────────────────────────────

    @PostMapping("/api/v1/products/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a review (verified purchase required)")
    public ReviewResponse create(
            @PathVariable Long productId,
            @RequestBody @Valid ReviewRequest request,
            @AuthenticationPrincipal User user) {
        return reviewService.create(productId, user.getId(), request);
    }

    // ── GET /api/v1/products/{productId}/reviews (public) ────────────────────

    @GetMapping("/api/v1/products/{productId}/reviews")
    @Operation(summary = "List reviews for a product (public, paginated)")
    public Page<ReviewResponse> findByProduct(
            @PathVariable Long productId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return reviewService.findByProduct(productId, pageable);
    }

    // ── GET /api/v1/users/me/reviews ──────────────────────────────────────────

    @GetMapping("/api/v1/users/me/reviews")
    @Operation(summary = "List own reviews (authenticated)")
    public Page<ReviewResponse> findMine(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User user) {
        return reviewService.findByUser(user.getId(), pageable);
    }

    // ── DELETE /api/v1/reviews/{id} ───────────────────────────────────────────

    @DeleteMapping("/api/v1/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a review (owner or ADMIN)")
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        reviewService.delete(id, user.getId(), user.getRole());
    }
}
