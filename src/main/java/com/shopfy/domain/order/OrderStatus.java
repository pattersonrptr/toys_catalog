package com.shopfy.domain.order;

/**
 * Ciclo de vida de um pedido.
 *
 * <pre>
 * PENDING → CONFIRMED → SHIPPED → DELIVERED
 *    └─────────────────────────────→ CANCELLED
 * </pre>
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
