-- V1__initial_schema.sql
-- Migração inicial: schema do Toys Catalog
-- Baseado no modelo original de 2017, modernizado para PostgreSQL

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    description    TEXT,
    brand          VARCHAR(100) NOT NULL,
    image_url      VARCHAR(500),
    price          NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    featured       BOOLEAN NOT NULL DEFAULT FALSE,
    category_id    BIGINT NOT NULL REFERENCES categories (id) ON DELETE RESTRICT,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP
);

CREATE INDEX idx_products_category  ON products (category_id);
CREATE INDEX idx_products_active    ON products (active);
CREATE INDEX idx_products_featured  ON products (featured);
CREATE INDEX idx_products_price     ON products (price);
