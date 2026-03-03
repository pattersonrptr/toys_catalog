package com.shopfy.api.v1.controller;

import com.shopfy.api.v1.dto.CartItemRequest;
import com.shopfy.api.v1.dto.CartResponse;
import com.shopfy.application.cart.CartService;
import com.shopfy.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart — authenticated customers")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    // ── GET /api/v1/cart ──────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get current user's cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Cart contents"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user.getId()));
    }

    // ── POST /api/v1/cart/items ───────────────────────────────────────────────

    @PostMapping("/items")
    @Operation(summary = "Add item to cart (or increment quantity if already present)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated cart"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Product not found"),
                    @ApiResponse(responseCode = "409", description = "Insufficient stock")
            })
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(user.getId(), request));
    }

    // ── PUT /api/v1/cart/items/{itemId} ───────────────────────────────────────

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update quantity of a cart item",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated cart"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Cart item not found"),
                    @ApiResponse(responseCode = "409", description = "Insufficient stock")
            })
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(user.getId(), itemId, request));
    }

    // ── DELETE /api/v1/cart/items/{itemId} ────────────────────────────────────

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated cart"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Cart item not found")
            })
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(user.getId(), itemId));
    }

    // ── DELETE /api/v1/cart ───────────────────────────────────────────────────

    @DeleteMapping
    @Operation(summary = "Clear all items from cart",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cart cleared"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }
}
