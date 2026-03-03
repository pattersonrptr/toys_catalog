-- V4__cart.sql
-- Carrinho de compras: um carrinho por usuário (criado lazy no primeiro item)

CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT uq_carts_user UNIQUE (user_id)   -- 1 carrinho ativo por usuário
);

CREATE TABLE cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT         NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id BIGINT         NOT NULL REFERENCES products (id) ON DELETE RESTRICT,
    quantity   INT            NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(10, 2) NOT NULL CHECK (unit_price > 0),  -- preço no momento da adição
    CONSTRAINT uq_cart_item UNIQUE (cart_id, product_id)         -- sem duplicatas por produto
);

CREATE INDEX idx_cart_items_cart    ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product ON cart_items (product_id);
