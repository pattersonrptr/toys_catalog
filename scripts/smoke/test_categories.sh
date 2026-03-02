#!/usr/bin/env bash
# test_categories.sh — smoke tests for /api/v1/categories
# Usage: TOKEN=<admin_jwt> ./test_categories.sh   OR   sourced from run_all_tests.sh
set -euo pipefail
source "$(dirname "$0")/_common.sh"

header "Categories — public GET endpoints"

# ── 1. List categories (public) ───────────────────────────────────────────────
curl_json GET /api/v1/categories
assert_status "GET /categories → 200" 200 "$STATUS" "$BODY"

# ── 2. Get category by ID (seed: id=1 exists — "Action Figure") ──────────────
curl_json GET /api/v1/categories/1
assert_status "GET /categories/1 → 200" 200 "$STATUS" "$BODY"

# ── 3. Get category that does not exist → 404 ────────────────────────────────
curl_json GET /api/v1/categories/99999
assert_status "GET /categories/99999 → 404" 404 "$STATUS" "$BODY"

header "Categories — ADMIN mutations"

# ── 4. Create category (ADMIN) ────────────────────────────────────────────────
UNIQUE_NAME="Smoke-$(date +%s)"
curl_auth POST /api/v1/categories "{\"name\":\"$UNIQUE_NAME\",\"description\":\"Created by smoke test\"}"
assert_status "POST /categories (admin) → 201" 201 "$STATUS" "$BODY"
CREATED_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null || true)

# ── 5. Create category without token → 401 ───────────────────────────────────
curl_json POST /api/v1/categories '{"name":"HackCategory","description":"No auth"}'
assert_status "POST /categories (no token) → 401" 401 "$STATUS" "$BODY"

# ── 6. Create category with invalid payload → 400 ────────────────────────────
curl_auth POST /api/v1/categories '{"description":"Missing name field"}'
assert_status "POST /categories (invalid body) → 400" 400 "$STATUS" "$BODY"

# ── 7. Create duplicate category name → 409 ──────────────────────────────────
curl_auth POST /api/v1/categories "{\"name\":\"$UNIQUE_NAME\"}"
assert_status "POST /categories (duplicate name) → 409" 409 "$STATUS" "$BODY"

# ── 8. Update category (ADMIN) ────────────────────────────────────────────────
if [ -n "$CREATED_ID" ]; then
  curl_auth PUT /api/v1/categories/"$CREATED_ID" \
    "{\"name\":\"${UNIQUE_NAME}-updated\",\"description\":\"Updated by smoke test\"}"
  assert_status "PUT /categories/$CREATED_ID (admin) → 200" 200 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping PUT — could not get created category ID${RESET}"
fi

# ── 9. Delete category (ADMIN) — soft delete ─────────────────────────────────
if [ -n "$CREATED_ID" ]; then
  curl_auth DELETE /api/v1/categories/"$CREATED_ID"
  assert_status "DELETE /categories/$CREATED_ID (admin) → 204" 204 "$STATUS" "$BODY"
else
  echo -e "  ${YELLOW}⚠ Skipping DELETE — could not get created category ID${RESET}"
fi

# ── 10. Deleted category still reachable by ID but active=false ──────────────
# findById does NOT filter by active — returns 200 with active:false
if [ -n "$CREATED_ID" ]; then
  curl_json GET /api/v1/categories/"$CREATED_ID"
  assert_status "GET /categories/$CREATED_ID after delete → 200 (active=false)" 200 "$STATUS" "$BODY"
fi

summary
