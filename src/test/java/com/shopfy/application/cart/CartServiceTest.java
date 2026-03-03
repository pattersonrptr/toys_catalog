package com.shopfy.application.cart;

import com.shopfy.api.v1.dto.CartItemRequest;
import com.shopfy.api.v1.exception.InsufficientStockException;
import com.shopfy.api.v1.exception.ResourceNotFoundException;
import com.shopfy.domain.cart.Cart;
import com.shopfy.domain.cart.CartItem;
import com.shopfy.domain.cart.CartItemRepository;
import com.shopfy.domain.cart.CartRepository;
import com.shopfy.domain.product.Product;
import com.shopfy.domain.product.ProductRepository;
import com.shopfy.domain.user.User;
import com.shopfy.domain.user.UserRepository;
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
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Mock CartItemRepository cartItemRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CartService cartService;

    User user;
    Product product;
    Cart cart;

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

        cart = Cart.builder()
                .id(100L)
                .user(user)
                .items(new ArrayList<>())
                .build();
    }

    // ── getCart ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCart creates cart lazily when none exists")
    void getCart_createsCartWhenAbsent() {
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        var response = cartService.getCart(1L);

        assertThat(response.id()).isEqualTo(100L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("getCart returns existing cart")
    void getCart_returnsExistingCart() {
        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));

        var response = cartService.getCart(1L);

        assertThat(response.id()).isEqualTo(100L);
        verify(cartRepository, never()).save(any());
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addItem adds new product to cart")
    void addItem_addsNewItem() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdWithItems(1L))
                .thenReturn(Optional.of(cart))   // first call (getOrCreateCart)
                .thenReturn(Optional.of(cart));  // second call (reloadCart)
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any())).thenReturn(cart);

        var response = cartService.addItem(1L, new CartItemRequest(10L, 2));

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(2);
        assertThat(response.total()).isEqualByComparingTo("391.96");
    }

    @Test
    @DisplayName("addItem increments quantity when product already in cart")
    void addItem_incrementsExistingItem() {
        CartItem existing = CartItem.builder()
                .id(200L).cart(cart).product(product)
                .quantity(1).unitPrice(new BigDecimal("195.98"))
                .build();
        cart.getItems().add(existing);

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.findByUserIdWithItems(1L))
                .thenReturn(Optional.of(cart))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L)).thenReturn(Optional.of(existing));
        when(cartRepository.save(any())).thenReturn(cart);

        cartService.addItem(1L, new CartItemRequest(10L, 2));

        assertThat(existing.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("addItem throws InsufficientStockException when stock is too low")
    void addItem_throwsWhenInsufficientStock() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(1L, new CartItemRequest(10L, 10)))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Dohko de Libra");
    }

    @Test
    @DisplayName("addItem throws ResourceNotFoundException for inactive product")
    void addItem_throwsForInactiveProduct() {
        product.setActive(false);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        // getOrCreateCart is never reached — product filter fails first; no cart stub needed

        assertThatThrownBy(() -> cartService.addItem(1L, new CartItemRequest(10L, 1)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeItem removes item that belongs to cart")
    void removeItem_removesItem() {
        CartItem item = CartItem.builder()
                .id(200L).cart(cart).product(product)
                .quantity(1).unitPrice(new BigDecimal("195.98"))
                .build();
        cart.getItems().add(item);

        when(cartRepository.findByUserIdWithItems(1L))
                .thenReturn(Optional.of(cart))
                .thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));
        when(cartRepository.save(any())).thenReturn(cart);

        var response = cartService.removeItem(1L, 200L);

        assertThat(cart.getItems()).isEmpty();
        assertThat(response.items()).isEmpty();
    }

    @Test
    @DisplayName("removeItem throws when item belongs to a different cart")
    void removeItem_throwsWhenItemNotInCart() {
        Cart otherCart = Cart.builder().id(999L).user(user).items(new ArrayList<>()).build();
        CartItem item = CartItem.builder().id(200L).cart(otherCart).product(product)
                .quantity(1).unitPrice(BigDecimal.TEN).build();

        when(cartRepository.findByUserIdWithItems(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(200L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.removeItem(1L, 200L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
