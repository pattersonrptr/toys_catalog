package com.shopfy.api.v1.dto;

import com.shopfy.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Body para PATCH /orders/{id}/status (ADMIN).
 */
public record OrderStatusRequest(
        @NotNull(message = "status is required")
        OrderStatus status
) {}
