#!/usr/bin/env bash
# run_all_tests.sh — runs every smoke-test script and prints a grand total
# Usage:  ./scripts/smoke/run_all_tests.sh [--base-url http://host:port]
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# ── colour codes ──────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'
YELLOW='\033[1;33m'; BOLD='\033[1m'; RESET='\033[0m'

# ── optional --base-url flag ──────────────────────────────────────────────────
while [[ $# -gt 0 ]]; do
  case "$1" in
    --base-url) export BASE_URL="$2"; shift 2 ;;
    *) echo "Unknown argument: $1"; exit 1 ;;
  esac
done
export BASE_URL="${BASE_URL:-http://localhost:8080}"

echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}${CYAN}║        Shopfy  —  Smoke Test Suite               ║${RESET}"
echo -e "${BOLD}${CYAN}║        Target: ${BASE_URL}${RESET}"
echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════════════╝${RESET}"
echo

# ── health check before running anything ────────────────────────────────────
echo -e "${CYAN}► Waiting for app to respond…${RESET}"
for i in $(seq 1 10); do
  HTTP=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null || true)
  if [[ "$HTTP" == "200" ]]; then
    echo -e "  ${GREEN}✓ App is up (attempt $i)${RESET}\n"
    break
  fi
  if [[ $i -eq 10 ]]; then
    echo -e "  ${RED}✗ App did not respond after 10 attempts — aborting${RESET}"
    exit 1
  fi
  echo -e "  ${YELLOW}  attempt $i — HTTP $HTTP — retrying in 3s…${RESET}"
  sleep 3
done

# ── grand total counters ──────────────────────────────────────────────────────
GRAND_PASS=0
GRAND_FAIL=0
declare -a SUITE_RESULTS=()

# ── helper: run one script as a child process, TOKEN passed via environment ──
run_suite() {
  local script="$1"
  local label="$2"

  echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
  echo -e "${BOLD}  Suite: ${label}${RESET}"
  echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

  # TOKEN and BASE_URL are exported; child inherits them via environment.
  # Run in a subshell so that set -e inside the script doesn't kill us.
  set +e
  bash "$script"
  local rc=$?
  set -e

  if [[ $rc -eq 0 ]]; then
    SUITE_RESULTS+=("${GREEN}✓ PASS${RESET}  ${label}")
  else
    SUITE_RESULTS+=("${RED}✗ FAIL${RESET}  ${label}")
  fi
  echo
}

# ── Step 1: auth (also exports TOKEN + CUSTOMER_TOKEN) ───────────────────────
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  Suite: Authentication (login + token capture)${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
set +e
# shellcheck source=test_auth.sh
source "${SCRIPT_DIR}/test_auth.sh"
AUTH_RC=$?
set -e
if [[ $AUTH_RC -eq 0 ]]; then
  SUITE_RESULTS+=("${GREEN}✓ PASS${RESET}  Authentication")
else
  SUITE_RESULTS+=("${RED}✗ FAIL${RESET}  Authentication")
fi
echo

# After sourcing test_auth.sh, TOKEN and CUSTOMER_TOKEN are in scope.
if [[ -z "${TOKEN:-}" ]]; then
  echo -e "${RED}✗ No TOKEN available after auth suite — cannot run protected tests.${RESET}"
  exit 1
fi

# ── Step 2: /me ───────────────────────────────────────────────────────────────
run_suite "${SCRIPT_DIR}/test_me.sh"           "User (/me)"

# ── Step 3: categories ────────────────────────────────────────────────────────
run_suite "${SCRIPT_DIR}/test_categories.sh"   "Categories"

# ── Step 4: products ──────────────────────────────────────────────────────────
run_suite "${SCRIPT_DIR}/test_products.sh"     "Products"

# ── Step 5: product images ────────────────────────────────────────────────────
run_suite "${SCRIPT_DIR}/test_images.sh"       "Product Images"

# ── Grand summary ─────────────────────────────────────────────────────────────
echo -e "${BOLD}${CYAN}╔══════════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}${CYAN}║                GRAND TOTAL                       ║${RESET}"
echo -e "${BOLD}${CYAN}╚══════════════════════════════════════════════════╝${RESET}"
for result in "${SUITE_RESULTS[@]}"; do
  echo -e "  ${result}"
done
echo

# Count failures from suite results
TOTAL_SUITES=${#SUITE_RESULTS[@]}
FAILED_SUITES=0
for result in "${SUITE_RESULTS[@]}"; do
  if echo "$result" | grep -q "FAIL"; then
    ((FAILED_SUITES++)) || true
  fi
done
PASSED_SUITES=$(( TOTAL_SUITES - FAILED_SUITES ))

echo -e "  Suites:  ${TOTAL_SUITES}   ${GREEN}Passed: ${PASSED_SUITES}${RESET}   ${RED}Failed: ${FAILED_SUITES}${RESET}"
echo

if [[ $FAILED_SUITES -eq 0 ]]; then
  echo -e "${BOLD}${GREEN}  ✅ All suites passed!${RESET}"
  exit 0
else
  echo -e "${BOLD}${RED}  ❌ ${FAILED_SUITES} suite(s) failed — check output above.${RESET}"
  exit 1
fi
