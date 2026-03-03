package com.shopfy.api.v1.controller;

import com.shopfy.api.v1.dto.OrderResponse;
import com.shopfy.api.v1.dto.OrderStatusRequest;
import com.shopfy.application.order.OrderService;
import com.shopfy.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management — checkout and order history")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    // ── POST /api/v1/orders — checkout ────────────────────────────────────────

    @PostMapping("/api/v1/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Checkout — convert cart into an order",
            description = "Creates an order from the authenticated user's cart. " +
                    "Deducts stock for each item and clears the cart.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order created"),
                    @ApiResponse(responseCode = "400", description = "Cart is empty"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "409", description = "Insufficient stock for one or more items")
            })
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(user.getId()));
    }

    // ── GET /api/v1/orders — customer's own orders ────────────────────────────

    @GetMapping("/api/v1/orders")
    @Operation(summary = "List authenticated user's orders (paginated)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of orders"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated")
            })
    public ResponseEntity<Page<OrderResponse>> listMyOrders(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.findByUser(user.getId(), pageable));
    }

    // ── GET /api/v1/orders/{id} — customer ───────────────────────────────────

    @GetMapping("/api/v1/orders/{id}")
    @Operation(summary = "Get a specific order (must belong to authenticated user)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order detail"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            })
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.findByIdForUser(id, user.getId()));
    }

    // ── GET /api/v1/admin/orders — all orders (ADMIN) ────────────────────────

    @GetMapping("/api/v1/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all orders — ADMIN only",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Page of all orders"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "403", description = "Not ADMIN")
            })
    public ResponseEntity<Page<OrderResponse>> listAllOrders(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(pageable));
    }

    // ── PATCH /api/v1/admin/orders/{id}/status — ADMIN ───────────────────────

    @PatchMapping("/api/v1/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status — ADMIN only",
            description = "Allowed transitions: PENDING→CONFIRMED→SHIPPED→DELIVERED. " +
                    "CANCELLED is always allowed (except from terminal states).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated order"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "401", description = "Not authenticated"),
                    @ApiResponse(responseCode = "403", description = "Not ADMIN"),
                    @ApiResponse(responseCode = "404", description = "Order not found"),
                    @ApiResponse(responseCode = "409", description = "Invalid status transition")
            })
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.status()));
    }
}
