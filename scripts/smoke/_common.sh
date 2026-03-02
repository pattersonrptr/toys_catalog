#!/usr/bin/env bash
# _common.sh — shared helpers for smoke tests
# Source this file at the top of every test script:
#   source "$(dirname "$0")/_common.sh"

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASS=0
FAIL=0
ERRORS=()

# ── colours ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

# ── helpers ───────────────────────────────────────────────────────────────────

# Print a section header
header() {
  echo -e "\n${CYAN}${BOLD}▶ $1${RESET}"
}

# assert_status <label> <expected_status> <actual_status> [body]
assert_status() {
  local label="$1" expected="$2" actual="$3" body="${4:-}"
  if [ "$actual" -eq "$expected" ]; then
    echo -e "  ${GREEN}✔ $label${RESET} (HTTP $actual)"
    PASS=$((PASS + 1))
  else
    echo -e "  ${RED}✘ $label${RESET} — expected HTTP $expected, got HTTP $actual"
    [ -n "$body" ] && echo -e "    ${YELLOW}Body: $body${RESET}"
    FAIL=$((FAIL + 1))
    ERRORS+=("$label: expected $expected got $actual")
  fi
}

# curl_json <method> <path> [body] [extra_curl_args...]
# Returns: sets $STATUS and $BODY
curl_json() {
  local method="$1" path="$2" body="${3:-}"
  shift 3 || shift $#
  local extra=("$@")

  local args=(-s -w "\n%{http_code}" -X "$method" "${BASE_URL}${path}"
               -H "Content-Type: application/json"
               "${extra[@]}")
  [ -n "$body" ] && args+=(-d "$body")

  local response
  response=$(curl "${args[@]}" 2>/dev/null)

  STATUS=$(echo "$response" | tail -n1)
  BODY=$(echo "$response" | head -n -1)
}

# curl_auth <method> <path> [body]  — adds Bearer token from $TOKEN
curl_auth() {
  curl_json "$1" "$2" "${3:-}" -H "Authorization: Bearer ${TOKEN:-}"
}

# summary — call at the end of each script
summary() {
  local total=$((PASS + FAIL))
  echo -e "\n${BOLD}Results: ${GREEN}${PASS}${RESET}/${BOLD}${total}${RESET} passed"
  if [ ${#ERRORS[@]} -gt 0 ]; then
    echo -e "${RED}Failures:${RESET}"
    for e in "${ERRORS[@]}"; do echo -e "  • $e"; done
  fi
  [ "$FAIL" -eq 0 ]   # exit 0 if all pass, 1 otherwise
}
