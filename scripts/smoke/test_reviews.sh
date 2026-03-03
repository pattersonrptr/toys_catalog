#!/usr/bin/env bash
# test_reviews.sh — smoke tests for product reviews & ratings
# Usage: TOKEN=<admin_jwt> CUSTOMER_TOKEN=<customer_jwt> ./test_reviews.sh
#        OR sourced/run from run_all_tests.sh
#
# Pre-condition: CUSTOMER_TOKEN user must have at least one DELIVERED order
# containing product id=1. The script promotes an order to DELIVERED via admin
# before testing review creation.
set -euo pipefail
source "$(dirname "$0")/_common.sh"

PRODUCT_ID=1

# ── helper: place an order as customer and promote it to DELIVERED ─────────────
setup_delivered_order() {
  # Clear cart, add item, checkout
  curl_json DELETE /api/v1/cart \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}" > /dev/null 2>&1 || true

  curl_json POST /api/v1/cart/items \
    "{\"productId\":${PRODUCT_ID},\"quantity\":1}" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}" > /dev/null

  curl_json POST /api/v1/orders \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
  local order_id
  order_id=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('id', ''))
" 2>/dev/null || true)

  if [ -z "${order_id:-}" ]; then
    echo -e "  ${YELLOW}⚠ Could not create order for delivered-order setup — skipping${RESET}"
    return
  fi

  # Admin: PENDING → CONFIRMED → SHIPPED → DELIVERED
  for status in CONFIRMED SHIPPED DELIVERED; do
    curl_auth PATCH "/api/v1/admin/orders/${order_id}/status" \
      "{\"status\":\"${status}\"}" > /dev/null
  done

  export SETUP_ORDER_ID="$order_id"
  echo -e "  ${GREEN}✔ Delivered order created (id=${order_id})${RESET}"
}

header "Reviews — public GET (no auth)"

# ── 1. GET reviews for a product (public) → 200 ───────────────────────────────
curl_json GET "/api/v1/products/${PRODUCT_ID}/reviews"
assert_status "GET /products/:id/reviews (no auth) → 200" 200 "$STATUS" "$BODY"

header "Reviews — unauthenticated mutations"

# ── 2. POST review without token → 401 ───────────────────────────────────────
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" '{"rating":5}'
assert_status "POST /products/:id/reviews (no token) → 401" 401 "$STATUS" "$BODY"

# ── 3. DELETE review without token → 401 ─────────────────────────────────────
curl_json DELETE "/api/v1/reviews/1"
assert_status "DELETE /reviews/:id (no token) → 401" 401 "$STATUS" "$BODY"

header "Reviews — verified purchase gate"

# ── 4. POST review without a delivered order → 409 ───────────────────────────
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" \
  '{"rating":5,"comment":"Should fail"}' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST review (no purchase) → 409" 409 "$STATUS" "$BODY"

header "Reviews — setup delivered order"

setup_delivered_order

header "Reviews — create review"

# ── 5. POST review after verified purchase → 201 ─────────────────────────────
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" \
  '{"rating":5,"comment":"Produto incrível!"}' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST /products/:id/reviews (verified) → 201" 201 "$STATUS" "$BODY"
REVIEW_ID=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('id', ''))
" 2>/dev/null || true)
export REVIEW_ID

REVIEW_RATING=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('rating', ''))
" 2>/dev/null || true)
if [ "${REVIEW_RATING:-}" = "5" ]; then
  echo -e "  ${GREEN}✔ Review has correct rating (5)${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ Expected rating 5, got ${REVIEW_RATING:-}${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("Review rating mismatch: expected 5 got ${REVIEW_RATING:-}")
fi

# ── 6. POST duplicate review → 409 ───────────────────────────────────────────
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" \
  '{"rating":3,"comment":"Duplicate"}' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST duplicate review → 409" 409 "$STATUS" "$BODY"

# ── 7. GET reviews now has at least 1 review ──────────────────────────────────
curl_json GET "/api/v1/products/${PRODUCT_ID}/reviews"
assert_status "GET /products/:id/reviews (with review) → 200" 200 "$STATUS" "$BODY"
REVIEW_COUNT=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('totalElements', d.get('numberOfElements', 0)))
" 2>/dev/null || true)
if [ "${REVIEW_COUNT:-0}" -ge 1 ]; then
  echo -e "  ${GREEN}✔ Product has ${REVIEW_COUNT} review(s)${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ Expected ≥1 review, got ${REVIEW_COUNT:-0}${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("Review count: expected ≥1 got ${REVIEW_COUNT:-0}")
fi

# ── 8. GET /products/:id returns avgRating ────────────────────────────────────
curl_json GET "/api/v1/products/${PRODUCT_ID}"
assert_status "GET /products/:id (with avgRating) → 200" 200 "$STATUS" "$BODY"
AVG=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('avgRating', ''))
" 2>/dev/null || true)
if python3 -c "import sys; v=float('${AVG:-0}'); sys.exit(0 if v > 0 else 1)" 2>/dev/null; then
  echo -e "  ${GREEN}✔ avgRating populated: ${AVG}${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ avgRating should be >0 after review, got ${AVG:-}${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("avgRating not populated: ${AVG:-}")
fi

# ── 9. GET /users/me/reviews → 200 ───────────────────────────────────────────
curl_json GET /api/v1/users/me/reviews \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "GET /users/me/reviews → 200" 200 "$STATUS" "$BODY"

# ── 10. POST review with invalid rating (0) → 400 ────────────────────────────
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" \
  '{"rating":0}' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST review (rating=0) → 400" 400 "$STATUS" "$BODY"

# ── 11. POST review with rating > 5 → 400 ────────────────────────────────────
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" \
  '{"rating":6}' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST review (rating=6) → 400" 400 "$STATUS" "$BODY"

header "Reviews — delete"

# ── 12. Customer cannot delete another user's review ─────────────────────────
# Use admin token to try deleting — in this test, the review belongs to customer
if [ -n "${REVIEW_ID:-}" ]; then
  # A different customer cannot delete (use admin user as "other customer" is not easy)
  # Instead, verify owner CAN delete
  curl_json DELETE "/api/v1/reviews/${REVIEW_ID}" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
  assert_status "DELETE /reviews/:id (owner) → 204" 204 "$STATUS" "$BODY"

  # ── 13. Review should be gone ──────────────────────────────────────────────
  curl_json DELETE "/api/v1/reviews/${REVIEW_ID}" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
  assert_status "DELETE /reviews/:id (already deleted) → 404" 404 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping delete tests (no review id captured)${RESET}"
fi

# ── 14. Admin can delete a review (re-create first) ──────────────────────────
# Re-create the review (customer can now post again since theirs was deleted)
curl_json POST "/api/v1/products/${PRODUCT_ID}/reviews" \
  '{"rating":4,"comment":"Re-created"}' \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
NEW_REVIEW_ID=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('id', ''))
" 2>/dev/null || true)

if [ -n "${NEW_REVIEW_ID:-}" ]; then
  curl_auth DELETE "/api/v1/reviews/${NEW_REVIEW_ID}"
  assert_status "DELETE /reviews/:id (admin) → 204" 204 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping admin delete test (could not re-create review)${RESET}"
fi

summary
