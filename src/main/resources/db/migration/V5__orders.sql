-- V5__orders.sql
-- Pedidos gerados pelo checkout do carrinho

CREATE TABLE orders (
    id         BIGSERIAL      PRIMARY KEY,
    user_id    BIGINT         NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    status     VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    total      NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    created_at TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT chk_order_status CHECK (status IN (
        'PENDING',      -- aguardando pagamento
        'CONFIRMED',    -- pagamento confirmado
        'SHIPPED',      -- despachado
        'DELIVERED',    -- entregue
        'CANCELLED'     -- cancelado
    ))
);

CREATE TABLE order_items (
    id         BIGSERIAL      PRIMARY KEY,
    order_id   BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    quantity   INT            NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price > 0),  -- preço no momento do pedido
    CONSTRAINT uq_order_item UNIQUE (order_id, product_id)
);

CREATE INDEX idx_orders_user       ON orders (user_id);
CREATE INDEX idx_orders_status     ON orders (status);
CREATE INDEX idx_order_items_order ON order_items (order_id);
