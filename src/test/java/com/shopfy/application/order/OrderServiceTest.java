package com.shopfy.application.order;

import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.api.v1.exception.InsufficientStockException;
import com.shopfy.api.v1.exception.ResourceNotFoundException;
import com.shopfy.application.cart.CartService;
import com.shopfy.domain.cart.Cart;
import com.shopfy.domain.cart.CartItem;
import com.shopfy.domain.order.Order;
import com.shopfy.domain.order.OrderRepository;
import com.shopfy.domain.order.OrderStatus;
import com.shopfy.domain.product.Product;
import com.shopfy.domain.product.ProductRepository;
import com.shopfy.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock CartService cartService;
    @InjectMocks OrderService orderService;

    User user;
    Product product;
    Cart cart;
    CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Alice").email("alice@example.com").build();

        product = Product.builder()
                .id(10L)
                .name("Dohko de Libra")
                .price(new BigDecimal("195.98"))
                .stockQuantity(5)
                .active(true)
                .build();

        cartItem = CartItem.builder()
                .id(200L)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("195.98"))
                .build();

        cart = Cart.builder()
                .id(100L)
                .user(user)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();

        cartItem.setCart(cart);
    }

    // ── checkout ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkout creates order, deducts stock, clears cart")
    void checkout_createsOrderAndDeductsStock() {
        Order finalOrder = Order.builder()
                .id(1L).user(user).total(new BigDecimal("391.96")).items(new ArrayList<>()).build();

        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        // save() sets id=1 on the returned object (mimics JPA id generation)
        when(orderRepository.save(any(Order.class))).thenReturn(finalOrder);
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(finalOrder));

        var response = orderService.checkout(1L);

        assertThat(response.total()).isEqualByComparingTo("391.96");
        assertThat(product.getStockQuantity()).isEqualTo(3);   // 5 - 2
        verify(cartService).clearCart(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("checkout throws BusinessException when cart is empty")
    void checkout_throwsWhenCartEmpty() {
        cart.getItems().clear();
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);

        assertThatThrownBy(() -> orderService.checkout(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    @DisplayName("checkout throws InsufficientStockException when stock is too low")
    void checkout_throwsWhenInsufficientStock() {
        product.setStockQuantity(1);   // only 1 available but cart wants 2
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.checkout(1L))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Dohko de Libra");

        verify(cartService, never()).clearCart(any());
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus changes PENDING to CONFIRMED")
    void updateStatus_pendingToConfirmed() {
        Order order = Order.builder()
                .id(1L).user(user).status(OrderStatus.PENDING)
                .total(BigDecimal.TEN).items(new ArrayList<>()).build();

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        var response = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateStatus throws when order is DELIVERED (terminal state)")
    void updateStatus_throwsForDeliveredOrder() {
        Order order = Order.builder()
                .id(1L).user(user).status(OrderStatus.DELIVERED)
                .total(BigDecimal.TEN).items(new ArrayList<>()).build();

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    @DisplayName("updateStatus throws when trying to revert to PENDING")
    void updateStatus_throwsWhenRevertingToPending() {
        Order order = Order.builder()
                .id(1L).user(user).status(OrderStatus.CONFIRMED)
                .total(BigDecimal.TEN).items(new ArrayList<>()).build();

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test
    @DisplayName("updateStatus throws ResourceNotFoundException when order not found")
    void updateStatus_throwsWhenOrderNotFound() {
        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(99L, OrderStatus.CONFIRMED))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
