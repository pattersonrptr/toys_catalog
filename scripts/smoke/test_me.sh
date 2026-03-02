#!/usr/bin/env bash
# test_me.sh — smoke tests for GET /api/v1/me
# Usage: TOKEN=<jwt> ./test_me.sh   OR   sourced from run_all_tests.sh
set -euo pipefail
source "$(dirname "$0")/_common.sh"

header "User — GET /api/v1/me"

# ── 1. Get own profile with valid token ───────────────────────────────────────
curl_auth GET /api/v1/me
assert_status "GET /me with token → 200" 200 "$STATUS" "$BODY"

# ── 2. No token → 401 ────────────────────────────────────────────────────────
curl_json GET /api/v1/me
assert_status "GET /me without token → 401" 401 "$STATUS" "$BODY"

# ── 3. Invalid token → 401 ───────────────────────────────────────────────────
curl_json GET /api/v1/me "" -H "Authorization: Bearer this.is.not.a.valid.token"
assert_status "GET /me with invalid token → 401" 401 "$STATUS" "$BODY"

summary
