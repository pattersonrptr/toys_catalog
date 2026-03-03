-- V6: product reviews & ratings
-- A user can review a product at most once (UNIQUE constraint).
-- Only users who have a DELIVERED order containing the product may submit a review
-- (enforced at the application layer, not by a DB constraint).

CREATE TABLE reviews (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id     BIGINT       NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    rating      SMALLINT     NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP,

    CONSTRAINT uq_review_product_user UNIQUE (product_id, user_id)
);

CREATE INDEX idx_reviews_product_id ON reviews (product_id);
CREATE INDEX idx_reviews_user_id    ON reviews (user_id);
