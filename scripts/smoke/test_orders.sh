#!/usr/bin/env bash
# test_orders.sh — smoke tests for /api/v1/orders and /api/v1/admin/orders
# Usage: TOKEN=<admin_jwt> CUSTOMER_TOKEN=<customer_jwt> ./test_orders.sh
#        OR sourced/run from run_all_tests.sh
set -euo pipefail
source "$(dirname "$0")/_common.sh"

PRODUCT_ID=1

# ── helper: add one item to the customer's cart ───────────────────────────────
add_item_to_cart() {
  local qty="${1:-2}"
  curl_json POST /api/v1/cart/items \
    "{\"productId\":${PRODUCT_ID},\"quantity\":${qty}}" \
    -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
}

header "Orders — unauthenticated access"

# ── 1. POST /orders without token → 401 ──────────────────────────────────────
curl_json POST /api/v1/orders
assert_status "POST /orders (no token) → 401" 401 "$STATUS" "$BODY"

# ── 2. GET /orders without token → 401 ───────────────────────────────────────
curl_json GET /api/v1/orders
assert_status "GET /orders (no token) → 401" 401 "$STATUS" "$BODY"

header "Orders — checkout"

# ── 3. Checkout with empty cart → 409 ────────────────────────────────────────
# First clear any leftover cart state
curl_json DELETE /api/v1/cart -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}" > /dev/null || true
curl_json POST /api/v1/orders -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST /orders (empty cart) → 409" 409 "$STATUS" "$BODY"

# ── 4. Add item to cart then checkout → 201 ──────────────────────────────────
add_item_to_cart 2
curl_json POST /api/v1/orders -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST /orders (checkout) → 201" 201 "$STATUS" "$BODY"
ORDER_ID=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('id', ''))
" 2>/dev/null || true)
export ORDER_ID

if [ -n "${ORDER_ID:-}" ]; then
  echo -e "    Created order id=${ORDER_ID}"
fi

# ── 5. Cart should be empty after checkout ────────────────────────────────────
curl_json GET /api/v1/cart -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "GET /cart (after checkout) → 200" 200 "$STATUS" "$BODY"
CART_ITEMS=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(len(d.get('items', [])))
" 2>/dev/null || true)
if [ "${CART_ITEMS:-1}" -eq 0 ]; then
  echo -e "  ${GREEN}✔ Cart cleared after checkout${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ Cart should be empty after checkout but has ${CART_ITEMS} item(s)${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("Cart not cleared after checkout: ${CART_ITEMS} items remain")
fi

header "Orders — customer read"

# ── 6. GET /orders (own orders) → 200 ────────────────────────────────────────
curl_json GET /api/v1/orders -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "GET /orders (customer) → 200" 200 "$STATUS" "$BODY"

# ── 7. GET /orders/:id (own order) → 200 ─────────────────────────────────────
if [ -n "${ORDER_ID:-}" ]; then
  curl_json GET "/api/v1/orders/${ORDER_ID}" -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
  assert_status "GET /orders/:id (own) → 200" 200 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping GET /orders/:id (no order id captured)${RESET}"
fi

# ── 8. GET /orders/:id of another user (use admin order id if any) → 404 ─────
curl_json GET "/api/v1/orders/999999" -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "GET /orders/999999 (not found) → 404" 404 "$STATUS" "$BODY"

# ── 9. Customer cannot access admin endpoint → 403 ───────────────────────────
curl_json GET /api/v1/admin/orders -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "GET /admin/orders (customer) → 403" 403 "$STATUS" "$BODY"

header "Orders — admin"

# ── 10. GET /admin/orders → 200 ──────────────────────────────────────────────
curl_auth GET /api/v1/admin/orders
assert_status "GET /admin/orders (admin) → 200" 200 "$STATUS" "$BODY"

# ── 11. PATCH /admin/orders/:id/status → 200 ─────────────────────────────────
if [ -n "${ORDER_ID:-}" ]; then
  curl_auth PATCH "/api/v1/admin/orders/${ORDER_ID}/status" '{"status":"CONFIRMED"}'
  assert_status "PATCH /admin/orders/:id/status → 200" 200 "$STATUS" "$BODY"

  # ── 12. Verify status updated ────────────────────────────────────────────────
  NEW_STATUS=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('status', ''))
" 2>/dev/null || true)
  if [ "${NEW_STATUS:-}" = "CONFIRMED" ]; then
    echo -e "  ${GREEN}✔ Order status updated to CONFIRMED${RESET}"
    PASS=$((PASS + 1))
  else
    echo -e "  ${RED}✘ Expected status CONFIRMED, got ${NEW_STATUS:-}${RESET}"
    FAIL=$((FAIL + 1))
    ERRORS+=("Order status update: expected CONFIRMED got ${NEW_STATUS:-}")
  fi

  # ── 13. PATCH with invalid status → 400 ──────────────────────────────────────
  curl_auth PATCH "/api/v1/admin/orders/${ORDER_ID}/status" '{"status":"INVALID_STATUS"}'
  assert_status "PATCH /admin/orders/:id/status (invalid) → 400" 400 "$STATUS" "$BODY"

  # ── 14. PATCH: revert to PENDING (invalid transition) → 409 ──────────────────
  curl_auth PATCH "/api/v1/admin/orders/${ORDER_ID}/status" '{"status":"PENDING"}'
  assert_status "PATCH status CONFIRMED→PENDING (invalid) → 409" 409 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping admin status tests (no order id captured)${RESET}"
fi

# ── 15. PATCH non-existent order → 404 ───────────────────────────────────────
curl_auth PATCH "/api/v1/admin/orders/999999/status" '{"status":"CONFIRMED"}'
assert_status "PATCH /admin/orders/999999/status → 404" 404 "$STATUS" "$BODY"

# ── 16. Checkout with insufficient stock → 409 ───────────────────────────────
# Add more items than available stock (seed product has 10 units; we already bought 2 above,
# so 8 remain — request 999 to guarantee overflow)
add_item_to_cart 999
curl_json POST /api/v1/orders -H "Authorization: Bearer ${CUSTOMER_TOKEN:-}"
assert_status "POST /orders (insufficient stock) → 409" 409 "$STATUS" "$BODY"

summary
