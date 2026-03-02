#!/usr/bin/env bash
# test_products.sh — smoke tests for /api/v1/products
# Usage: TOKEN=<admin_jwt> ./test_products.sh   OR   sourced from run_all_tests.sh
set -euo pipefail
source "$(dirname "$0")/_common.sh"

header "Products — public GET endpoints"

# ── 1. List products (public) ─────────────────────────────────────────────────
curl_json GET /api/v1/products
assert_status "GET /products → 200" 200 "$STATUS" "$BODY"

# ── 2. List with pagination params ────────────────────────────────────────────
curl_json GET "/api/v1/products?page=0&size=3&sort=price,asc"
assert_status "GET /products?page=0&size=3 → 200" 200 "$STATUS" "$BODY"

# ── 3. Get product by ID (seed: id=2 exists) ──────────────────────────────────
curl_json GET /api/v1/products/2
assert_status "GET /products/2 → 200" 200 "$STATUS" "$BODY"

# ── 4. Get product that does not exist → 404 ─────────────────────────────────
curl_json GET /api/v1/products/99999
assert_status "GET /products/99999 → 404" 404 "$STATUS" "$BODY"

# ── 5. List featured products ─────────────────────────────────────────────────
curl_json GET /api/v1/products/featured
assert_status "GET /products/featured → 200" 200 "$STATUS" "$BODY"

# ── 6. List by category (seed: category id=2 exists) ─────────────────────────
curl_json GET /api/v1/products/category/2
assert_status "GET /products/category/2 → 200" 200 "$STATUS" "$BODY"

# ── 7. Search by keyword ──────────────────────────────────────────────────────
curl_json GET "/api/v1/products/search?q=action"
assert_status "GET /products/search?q=action → 200" 200 "$STATUS" "$BODY"

header "Products — ADMIN mutations"

# ── 8. Create product (ADMIN) ─────────────────────────────────────────────────
curl_auth POST /api/v1/products '{
  "name": "Smoke Test Product",
  "description": "Created by smoke test",
  "brand": "TestBrand",
  "price": 49.99,
  "stockQuantity": 10,
  "categoryId": 1
}'
assert_status "POST /products (admin) → 201" 201 "$STATUS" "$BODY"
CREATED_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null || true)

# ── 9. Create product without token → 401 ────────────────────────────────────
curl_json POST /api/v1/products '{"name":"Hack","price":1,"categoryId":1,"brand":"x","stockQuantity":0}'
assert_status "POST /products (no token) → 401" 401 "$STATUS" "$BODY"

# ── 10. Create product with missing required fields → 400 ────────────────────
curl_auth POST /api/v1/products '{"name":"Incomplete"}'
assert_status "POST /products (invalid body) → 400" 400 "$STATUS" "$BODY"

# ── 11. Update product (ADMIN) ────────────────────────────────────────────────
if [ -n "$CREATED_ID" ]; then
  curl_auth PUT /api/v1/products/"$CREATED_ID" '{
    "name": "Smoke Test Product (updated)",
    "description": "Updated by smoke test",
    "brand": "TestBrand",
    "price": 59.99,
    "stockQuantity": 5,
    "categoryId": 1
  }'
  assert_status "PUT /products/$CREATED_ID (admin) → 200" 200 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping PUT — could not get created product ID${RESET}"
fi

# ── 12. Delete product (ADMIN) — soft delete ─────────────────────────────────
if [ -n "$CREATED_ID" ]; then
  curl_auth DELETE /api/v1/products/"$CREATED_ID"
  assert_status "DELETE /products/$CREATED_ID (admin) → 204" 204 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping DELETE — could not get created product ID${RESET}"
fi

summary
