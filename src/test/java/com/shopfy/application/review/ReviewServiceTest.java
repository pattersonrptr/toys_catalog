package com.shopfy.application.review;

import com.shopfy.api.v1.dto.ReviewRequest;
import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.api.v1.exception.ResourceNotFoundException;
import com.shopfy.domain.order.OrderRepository;
import com.shopfy.domain.product.Product;
import com.shopfy.domain.product.ProductRepository;
import com.shopfy.domain.review.Review;
import com.shopfy.domain.review.ReviewRepository;
import com.shopfy.domain.user.Role;
import com.shopfy.domain.user.User;
import com.shopfy.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService")
class ReviewServiceTest {

    @Mock ReviewRepository reviewRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @Mock OrderRepository orderRepository;
    @InjectMocks ReviewService reviewService;

    User user;
    User admin;
    Product product;
    Review review;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Alice").email("alice@example.com").role(Role.CUSTOMER).build();
        admin = User.builder().id(99L).name("Admin").email("admin@shopfy.com").role(Role.ADMIN).build();

        product = Product.builder()
                .id(10L)
                .name("Dohko de Libra")
                .price(new BigDecimal("195.98"))
                .stockQuantity(5)
                .active(true)
                .build();

        review = Review.builder()
                .id(1L)
                .product(product)
                .user(user)
                .rating(5)
                .comment("Excelente!")
                .build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: saves review when user has a delivered order with the product")
    void create_savesReview() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsDeliveredOrderContainingProduct(1L, 10L)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserId(10L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        var response = reviewService.create(10L, 1L, new ReviewRequest(5, "Excelente!"));

        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Excelente!");
        assertThat(response.userId()).isEqualTo(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("create: throws ResourceNotFoundException for unknown product")
    void create_throwsForUnknownProduct() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(999L, 1L, new ReviewRequest(5, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("create: throws BusinessException when user has not purchased the product")
    void create_throwsWhenNotPurchased() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsDeliveredOrderContainingProduct(1L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.create(10L, 1L, new ReviewRequest(4, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("purchased");
    }

    @Test
    @DisplayName("create: throws BusinessException when user already reviewed the product")
    void create_throwsOnDuplicateReview() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.existsDeliveredOrderContainingProduct(1L, 10L)).thenReturn(true);
        when(reviewRepository.existsByProductIdAndUserId(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(10L, 1L, new ReviewRequest(3, "Again")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already reviewed");
    }

    // ── findByProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByProduct: returns page of reviews for active product")
    void findByProduct_returnsPage() {
        var pageable = PageRequest.of(0, 10);
        when(productRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByProductId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(review)));

        var result = reviewService.findByProduct(10L, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().rating()).isEqualTo(5);
    }

    @Test
    @DisplayName("findByProduct: throws ResourceNotFoundException for unknown product")
    void findByProduct_throwsForUnknownProduct() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.findByProduct(999L, PageRequest.of(0, 10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: owner can delete own review")
    void delete_ownerCanDelete() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.delete(1L, user.getId(), Role.CUSTOMER);

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("delete: ADMIN can delete any review")
    void delete_adminCanDeleteAnyReview() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        reviewService.delete(1L, admin.getId(), Role.ADMIN);

        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("delete: throws BusinessException when non-owner non-admin tries to delete")
    void delete_throwsWhenNotOwnerOrAdmin() {
        User other = User.builder().id(50L).role(Role.CUSTOMER).build();
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.delete(1L, other.getId(), Role.CUSTOMER))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not allowed");

        verify(reviewRepository, never()).delete(any());
    }
}
