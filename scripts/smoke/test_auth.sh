#!/usr/bin/env bash
# test_auth.sh — smoke tests for POST /auth/register and POST /auth/login
# Usage: ./test_auth.sh
# Exports: TOKEN (JWT for admin), CUSTOMER_TOKEN (JWT for registered customer)
set -euo pipefail
source "$(dirname "$0")/_common.sh"

header "Auth — register"

# ── 1. Register a new customer ────────────────────────────────────────────────
curl_json POST /api/v1/auth/register '{"name":"Smoke Test User","email":"smoke@shopfy.com","password":"Smoke@123"}'
assert_status "Register new customer → 200" 200 "$STATUS" "$BODY"
CUSTOMER_TOKEN=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('accessToken',''))" 2>/dev/null || true)

# ── 2. Register duplicate email → 409 ────────────────────────────────────────
curl_json POST /api/v1/auth/register '{"name":"Dup","email":"smoke@shopfy.com","password":"Smoke@123"}'
assert_status "Register duplicate email → 409" 409 "$STATUS" "$BODY"

# ── 3. Register with invalid data → 400 ──────────────────────────────────────
curl_json POST /api/v1/auth/register '{"name":"","email":"not-an-email","password":"x"}'
assert_status "Register invalid payload → 400" 400 "$STATUS" "$BODY"

header "Auth — login"

# ── 4. Login with default admin ───────────────────────────────────────────────
curl_json POST /api/v1/auth/login '{"email":"admin@shopfy.com","password":"Admin@123"}'
assert_status "Login admin → 200" 200 "$STATUS" "$BODY"
TOKEN=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('accessToken',''))" 2>/dev/null || true)
if [ -z "$TOKEN" ]; then
  echo -e "  ${RED}✘ Could not extract admin token — subsequent tests will fail${RESET}"
else
  echo -e "    Token: ${TOKEN:0:40}..."
fi
export TOKEN

# ── 5. Login with registered customer ────────────────────────────────────────
curl_json POST /api/v1/auth/login '{"email":"smoke@shopfy.com","password":"Smoke@123"}'
assert_status "Login customer → 200" 200 "$STATUS" "$BODY"
export CUSTOMER_TOKEN

# ── 6. Login with wrong password → 401 ───────────────────────────────────────
curl_json POST /api/v1/auth/login '{"email":"admin@shopfy.com","password":"wrongpassword"}'
assert_status "Login wrong password → 401" 401 "$STATUS" "$BODY"

# ── 7. Login with unknown email → 401 ────────────────────────────────────────
curl_json POST /api/v1/auth/login '{"email":"nobody@shopfy.com","password":"anything"}'
assert_status "Login unknown email → 401" 401 "$STATUS" "$BODY"

summary
