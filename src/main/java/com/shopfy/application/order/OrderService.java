package com.shopfy.application.order;

import com.shopfy.api.v1.dto.OrderResponse;
import com.shopfy.api.v1.exception.BusinessException;
import com.shopfy.api.v1.exception.InsufficientStockException;
import com.shopfy.api.v1.exception.ResourceNotFoundException;
import com.shopfy.application.cart.CartService;
import com.shopfy.domain.cart.Cart;
import com.shopfy.domain.order.Order;
import com.shopfy.domain.order.OrderItem;
import com.shopfy.domain.order.OrderRepository;
import com.shopfy.domain.order.OrderStatus;
import com.shopfy.domain.product.Product;
import com.shopfy.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    // ── POST /orders — checkout ───────────────────────────────────────────────

    @Transactional
    public OrderResponse checkout(Long userId) {
        Cart cart = cartService.getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cannot checkout with an empty cart");
        }

        // Build order items, validate & deduct stock — all in the same transaction
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", cartItem.getProduct().getId()));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), cartItem.getQuantity(), product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            return OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();
        }).toList();

        BigDecimal total = orderItems.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(cart.getUser())
                .total(total)
                .build();

        // Wire bidirectional relationship
        orderItems.forEach(item -> {
            item.setOrder(order);
            order.getItems().add(item);
        });

        Order saved = orderRepository.save(order);

        // Clear cart after successful checkout
        cartService.clearCart(userId);

        return OrderResponse.from(
                orderRepository.findByIdWithItems(saved.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Order", saved.getId())));
    }

    // ── GET /orders — customer sees only their own ────────────────────────────

    public Page<OrderResponse> findByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(order -> OrderResponse.from(loadItems(order)));
    }

    // ── GET /orders/{id} — customer ───────────────────────────────────────────

    public OrderResponse findByIdForUser(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }

    // ── GET /admin/orders — all orders ───────────────────────────────────────

    public Page<OrderResponse> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(order -> OrderResponse.from(loadItems(order)));
    }

    // ── PATCH /admin/orders/{id}/status — ADMIN ───────────────────────────────

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        orderRepository.save(order);
        return OrderResponse.from(order);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Loads items for an order that was fetched without a JOIN FETCH
     * (e.g. from a paginated query that returns plain Order objects).
     */
    private Order loadItems(Order order) {
        return orderRepository.findByIdWithItems(order.getId()).orElse(order);
    }

    /**
     * Basic state-machine guard — prevents nonsensical transitions.
     * DELIVERED and CANCELLED are terminal states.
     */
    private void validateTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new BusinessException(
                    "Cannot change status of a " + current + " order");
        }
        if (next == OrderStatus.PENDING) {
            throw new BusinessException("Cannot revert order to PENDING");
        }
    }
}
