#!/usr/bin/env bash
# test_images.sh — smoke tests for product image upload / removal
# Usage:  TOKEN=<admin_jwt> ./test_images.sh
#         Sourced automatically by run_all_tests.sh
#
# Requires: curl, python3, file command (for creating a minimal JPEG/PNG)
# MinIO must be running and the shopfy-images bucket must exist.
set -euo pipefail
source "$(dirname "$0")/_common.sh"

# ── helpers ───────────────────────────────────────────────────────────────────

# curl_multipart <token> <method> <path> [form_field]
# Sets $STATUS and $BODY
# form_field is optional — omit for DELETE (no body)
curl_multipart() {
  local token="$1" method="$2" path="$3" form_field="${4:-}"
  local response
  local args=(-s -w "\n%{http_code}" -X "$method" "${BASE_URL}${path}"
              -H "Authorization: Bearer ${token}")
  [ -n "$form_field" ] && args+=(-F "${form_field}")
  response=$(curl "${args[@]}" 2>/dev/null)
  STATUS=$(echo "$response" | tail -n1)
  BODY=$(echo "$response" | head -n -1)
}

# ── create temporary test image files ────────────────────────────────────────
TMPDIR_IMAGES=$(mktemp -d)
trap 'rm -rf "$TMPDIR_IMAGES"' EXIT

# Minimal valid JPEG (JFIF, 1×1 pixel, 631 bytes)
JPEG_FILE="${TMPDIR_IMAGES}/test.jpg"
python3 - <<'PYEOF' > "$JPEG_FILE"
import sys
# Minimal 1x1 white JPEG produced by Pillow, embedded as literal bytes
data = bytes([
  0xFF,0xD8,0xFF,0xE0,0x00,0x10,0x4A,0x46,0x49,0x46,0x00,0x01,0x01,0x00,0x00,0x01,
  0x00,0x01,0x00,0x00,0xFF,0xDB,0x00,0x43,0x00,0x08,0x06,0x06,0x07,0x06,0x05,0x08,
  0x07,0x07,0x07,0x09,0x09,0x08,0x0A,0x0C,0x14,0x0D,0x0C,0x0B,0x0B,0x0C,0x19,0x12,
  0x13,0x0F,0x14,0x1D,0x1A,0x1F,0x1E,0x1D,0x1A,0x1C,0x1C,0x20,0x24,0x2E,0x27,0x20,
  0x22,0x2C,0x23,0x1C,0x1C,0x28,0x37,0x29,0x2C,0x30,0x31,0x34,0x34,0x34,0x1F,0x27,
  0x39,0x3D,0x38,0x32,0x3C,0x2E,0x33,0x34,0x32,0xFF,0xC0,0x00,0x0B,0x08,0x00,0x01,
  0x00,0x01,0x01,0x01,0x11,0x00,0xFF,0xC4,0x00,0x1F,0x00,0x00,0x01,0x05,0x01,0x01,
  0x01,0x01,0x01,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x02,0x03,0x04,
  0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0xFF,0xC4,0x00,0xB5,0x10,0x00,0x02,0x01,0x03,
  0x03,0x02,0x04,0x03,0x05,0x05,0x04,0x04,0x00,0x00,0x01,0x7D,0x01,0x02,0x03,0x00,
  0x04,0x11,0x05,0x12,0x21,0x31,0x41,0x06,0x13,0x51,0x61,0x07,0x22,0x71,0x14,0x32,
  0x81,0x91,0xA1,0x08,0x23,0x42,0xB1,0xC1,0x15,0x52,0xD1,0xF0,0x24,0x33,0x62,0x72,
  0x82,0x09,0x0A,0x16,0x17,0x18,0x19,0x1A,0x25,0x26,0x27,0x28,0x29,0x2A,0x34,0x35,
  0x36,0x37,0x38,0x39,0x3A,0x43,0x44,0x45,0x46,0x47,0x48,0x49,0x4A,0x53,0x54,0x55,
  0x56,0x57,0x58,0x59,0x5A,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x6A,0x73,0x74,0x75,
  0x76,0x77,0x78,0x79,0x7A,0x83,0x84,0x85,0x86,0x87,0x88,0x89,0x8A,0x92,0x93,0x94,
  0x95,0x96,0x97,0x98,0x99,0x9A,0xA2,0xA3,0xA4,0xA5,0xA6,0xA7,0xA8,0xA9,0xAA,0xB2,
  0xB3,0xB4,0xB5,0xB6,0xB7,0xB8,0xB9,0xBA,0xC2,0xC3,0xC4,0xC5,0xC6,0xC7,0xC8,0xC9,
  0xCA,0xD2,0xD3,0xD4,0xD5,0xD6,0xD7,0xD8,0xD9,0xDA,0xE1,0xE2,0xE3,0xE4,0xE5,0xE6,
  0xE7,0xE8,0xE9,0xEA,0xF1,0xF2,0xF3,0xF4,0xF5,0xF6,0xF7,0xF8,0xF9,0xFA,0xFF,0xDA,
  0x00,0x08,0x01,0x01,0x00,0x00,0x3F,0x00,0xFB,0xD2,0x8A,0x28,0x03,0xFF,0xD9
])
sys.stdout.buffer.write(data)
PYEOF

# Plain text file to test MIME rejection
TXT_FILE="${TMPDIR_IMAGES}/test.txt"
echo "This is not an image" > "$TXT_FILE"

# ── resolve a stable product ID for testing ──────────────────────────────────
# Use product id=1 from seed data (always present)
TEST_PRODUCT_ID=1

# ── tests ─────────────────────────────────────────────────────────────────────

header "Product Images — unauthenticated attempts"

# ── 1. Upload without token → 401 ────────────────────────────────────────────
STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST "${BASE_URL}/api/v1/products/${TEST_PRODUCT_ID}/image" \
  -F "file=@${JPEG_FILE}" 2>/dev/null)
assert_status "POST /products/${TEST_PRODUCT_ID}/image (no token) → 401" 401 "$STATUS"

# ── 2. Delete without token → 401 ────────────────────────────────────────────
STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -X DELETE "${BASE_URL}/api/v1/products/${TEST_PRODUCT_ID}/image" 2>/dev/null)
assert_status "DELETE /products/${TEST_PRODUCT_ID}/image (no token) → 401" 401 "$STATUS"

header "Product Images — ADMIN upload"

# ── 3. Upload valid JPEG (ADMIN) → 200 ───────────────────────────────────────
curl_multipart "$TOKEN" POST "/api/v1/products/${TEST_PRODUCT_ID}/image" \
  "file=@${JPEG_FILE};type=image/jpeg"
assert_status "POST /products/${TEST_PRODUCT_ID}/image (JPEG, admin) → 200" 200 "$STATUS" "$BODY"

# Capture the returned URL
IMAGE_URL=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('imageUrl',''))" 2>/dev/null || true)
if [ -n "$IMAGE_URL" ]; then
  echo -e "  ${YELLOW}→ imageUrl: ${IMAGE_URL}${RESET}"
fi

# ── 4. GET /products/{id} reflects the new imageUrl ──────────────────────────
curl_json GET "/api/v1/products/${TEST_PRODUCT_ID}"
assert_status "GET /products/${TEST_PRODUCT_ID} after upload → 200" 200 "$STATUS" "$BODY"
STORED_URL=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('imageUrl',''))" 2>/dev/null || true)
if [ -n "$STORED_URL" ]; then
  echo -e "  ${YELLOW}→ stored imageUrl: ${STORED_URL}${RESET}"
  # ── 5. Public image URL is accessible (no auth required) ─────────────────
  IMG_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$STORED_URL" 2>/dev/null || true)
  assert_status "GET <imageUrl> publicly accessible → 200" 200 "$IMG_STATUS"
else
  echo -e "  ${YELLOW}⚠ Skipping public URL test — no imageUrl in GET response${RESET}"
fi

header "Product Images — MIME validation"

# ── 6. Upload plain text file → 409 (BusinessException / invalid MIME) ───────
curl_multipart "$TOKEN" POST "/api/v1/products/${TEST_PRODUCT_ID}/image" \
  "file=@${TXT_FILE};type=text/plain"
assert_status "POST /products/${TEST_PRODUCT_ID}/image (text/plain) → 409" 409 "$STATUS" "$BODY"

header "Product Images — replace existing"

# ── 7. Upload again to replace (ADMIN) → 200 ─────────────────────────────────
curl_multipart "$TOKEN" POST "/api/v1/products/${TEST_PRODUCT_ID}/image" \
  "file=@${JPEG_FILE};type=image/jpeg"
assert_status "POST /products/${TEST_PRODUCT_ID}/image (replace, admin) → 200" 200 "$STATUS" "$BODY"

header "Product Images — DELETE"

# ── 8. Delete image (ADMIN) → 204 ────────────────────────────────────────────
curl_multipart "$TOKEN" DELETE "/api/v1/products/${TEST_PRODUCT_ID}/image"
assert_status "DELETE /products/${TEST_PRODUCT_ID}/image (admin) → 204" 204 "$STATUS"

# ── 9. GET after delete — imageUrl should be null/absent ─────────────────────
curl_json GET "/api/v1/products/${TEST_PRODUCT_ID}"
assert_status "GET /products/${TEST_PRODUCT_ID} after delete → 200" 200 "$STATUS" "$BODY"
AFTER_URL=$(echo "$BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('imageUrl') or '')" 2>/dev/null || true)
if [ -z "$AFTER_URL" ]; then
  echo -e "  ${GREEN}✔ imageUrl is cleared after delete${RESET}"
  PASS=$((PASS + 1))
else
  echo -e "  ${RED}✘ imageUrl still set after delete: ${AFTER_URL}${RESET}"
  FAIL=$((FAIL + 1))
  ERRORS+=("imageUrl not cleared after DELETE")
fi

# ── 10. Delete again (idempotent) → 204 ───────────────────────────────────────
curl_multipart "$TOKEN" DELETE "/api/v1/products/${TEST_PRODUCT_ID}/image"
assert_status "DELETE /products/${TEST_PRODUCT_ID}/image (idempotent) → 204" 204 "$STATUS"

# ── 11. Upload on non-existent product → 404 ─────────────────────────────────
curl_multipart "$TOKEN" POST "/api/v1/products/99999/image" \
  "file=@${JPEG_FILE};type=image/jpeg"
assert_status "POST /products/99999/image (not found) → 404" 404 "$STATUS" "$BODY"

summary
