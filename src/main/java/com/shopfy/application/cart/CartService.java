package com.shopfy.application.cart;

import com.shopfy.api.v1.dto.CartItemRequest;
import com.shopfy.api.v1.dto.CartResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ── GET /cart ─────────────────────────────────────────────────────────────

    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return CartResponse.from(cart);
    }

    // ── POST /cart/items ──────────────────────────────────────────────────────

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        validateStock(product, request.quantity());

        Cart cart = getOrCreateCart(userId);

        // If the product is already in the cart, update quantity
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .ifPresentOrElse(existing -> {
                    int newQty = existing.getQuantity() + request.quantity();
                    validateStock(product, newQty);
                    existing.setQuantity(newQty);
                }, () -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(request.quantity())
                            .unitPrice(product.getPrice())
                            .build();
                    cart.addItem(newItem);
                });

        cartRepository.save(cart);
        return CartResponse.from(reloadCart(userId));
    }

    // ── PUT /cart/items/{itemId} ──────────────────────────────────────────────

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return CartResponse.from(reloadCart(userId));
    }

    // ── DELETE /cart/items/{itemId} ───────────────────────────────────────────

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .filter(i -> i.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", itemId));

        cart.removeItem(item);
        cartRepository.save(cart);
        return CartResponse.from(reloadCart(userId));
    }

    // ── DELETE /cart (clear) ──────────────────────────────────────────────────

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByUserIdWithItems(userId).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }

    // ── Package-level helper used by OrderService ─────────────────────────────

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Cart reloadCart(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", userId));
    }

    private void validateStock(Product product, int requestedQty) {
        if (product.getStockQuantity() < requestedQty) {
            throw new InsufficientStockException(
                    product.getName(), requestedQty, product.getStockQuantity());
        }
    }
}
