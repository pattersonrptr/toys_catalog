#!/usr/bin/env bash
# test_cart.sh — smoke tests for /api/v1/cart
# Usage: TOKEN=<jwt> ./test_cart.sh   OR   sourced from run_all_tests.sh
set -euo pipefail
source "$(dirname "$0")/_common.sh"

# ── Ensure a product exists that we can add ───────────────────────────────────
# We assume seed data has at least product id=1 with stock > 0.
PRODUCT_ID=1

header "Cart — unauthenticated access"

# ── 1. GET cart without token → 401 ──────────────────────────────────────────
curl_json GET /api/v1/cart
assert_status "GET /cart (no token) → 401" 401 "$STATUS" "$BODY"

# ── 2. POST cart/items without token → 401 ───────────────────────────────────
curl_json POST /api/v1/cart/items "{\"productId\":${PRODUCT_ID},\"quantity\":1}"
assert_status "POST /cart/items (no token) → 401" 401 "$STATUS" "$BODY"

header "Cart — authenticated customer"

# ── 3. GET empty cart (auto-created) → 200 ───────────────────────────────────
curl_auth GET /api/v1/cart
assert_status "GET /cart (empty) → 200" 200 "$STATUS" "$BODY"

# ── 4. Add item to cart → 200 ────────────────────────────────────────────────
curl_auth POST /api/v1/cart/items "{\"productId\":${PRODUCT_ID},\"quantity\":2}"
assert_status "POST /cart/items → 200" 200 "$STATUS" "$BODY"
ITEM_ID=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
items = d.get('items', [])
print(items[0]['id'] if items else '')
" 2>/dev/null || true)
export CART_ITEM_ID="$ITEM_ID"

# ── 5. GET cart with items → 200 and contains product ────────────────────────
curl_auth GET /api/v1/cart
assert_status "GET /cart (with item) → 200" 200 "$STATUS" "$BODY"
ITEM_COUNT=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(len(d.get('items', [])))
" 2>/dev/null || true)
if [ "${ITEM_COUNT:-0}" -ge 1 ]; then
  echo -e "  ${GREEN}✔ Cart contains ${ITEM_COUNT} item(s)${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ Cart should have items but got ${ITEM_COUNT}${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("Cart item count: expected ≥1 got ${ITEM_COUNT:-0}")
fi

# ── 6. Add same product again → should increment quantity ────────────────────
curl_auth POST /api/v1/cart/items "{\"productId\":${PRODUCT_ID},\"quantity\":1}"
assert_status "POST /cart/items (duplicate → increment) → 200" 200 "$STATUS" "$BODY"

# ── 7. Update item quantity → 200 ────────────────────────────────────────────
if [ -n "${CART_ITEM_ID:-}" ]; then
  curl_auth PUT "/api/v1/cart/items/${CART_ITEM_ID}" '{"productId":'"${PRODUCT_ID}"',"quantity":5}'
  assert_status "PUT /cart/items/:id → 200" 200 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping PUT (no item id captured)${RESET}"
fi

# ── 8. Add item with invalid quantity → 400 ──────────────────────────────────
curl_auth POST /api/v1/cart/items "{\"productId\":${PRODUCT_ID},\"quantity\":0}"
assert_status "POST /cart/items (quantity=0) → 400" 400 "$STATUS" "$BODY"

# ── 9. Add item for non-existent product → 404 ───────────────────────────────
curl_auth POST /api/v1/cart/items '{"productId":999999,"quantity":1}'
assert_status "POST /cart/items (unknown product) → 404" 404 "$STATUS" "$BODY"

# ── 10. Remove specific item → 200 ───────────────────────────────────────────
if [ -n "${CART_ITEM_ID:-}" ]; then
  curl_auth DELETE "/api/v1/cart/items/${CART_ITEM_ID}"
  assert_status "DELETE /cart/items/:id → 200" 200 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping DELETE item (no item id captured)${RESET}"
fi

# ── 11. Clear cart → 200 ─────────────────────────────────────────────────────
# Re-add an item first so the cart is non-empty
curl_auth POST /api/v1/cart/items "{\"productId\":${PRODUCT_ID},\"quantity\":1}" > /dev/null
curl_auth DELETE /api/v1/cart
assert_status "DELETE /cart (clear) → 200" 200 "$STATUS" "$BODY"

# ── 12. Cart should be empty after clear ─────────────────────────────────────
curl_auth GET /api/v1/cart
assert_status "GET /cart (after clear) → 200" 200 "$STATUS" "$BODY"
ITEM_COUNT_AFTER=$(echo "$BODY" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(len(d.get('items', [])))
" 2>/dev/null || true)
if [ "${ITEM_COUNT_AFTER:-1}" -eq 0 ]; then
  echo -e "  ${GREEN}✔ Cart is empty after clear${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ Cart should be empty after clear but has ${ITEM_COUNT_AFTER} items${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("Cart not empty after clear: ${ITEM_COUNT_AFTER} items remain")
fi

summary
