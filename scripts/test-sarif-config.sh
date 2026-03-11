#!/usr/bin/env bash
# TDD validation tests for SARIF upload configuration (Issue #10)
# Tests verify that build.gradle.kts and ci.yml have the required SARIF config.

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

PASS=0
FAIL=0

assert_contains() {
    local file="$1"
    local pattern="$2"
    local description="$3"

    if grep -qP "$pattern" "$file" 2>/dev/null || grep -q "$pattern" "$file" 2>/dev/null; then
        echo "  PASS: $description"
        ((PASS++))
    else
        echo "  FAIL: $description"
        echo "        Expected pattern '$pattern' in $file"
        ((FAIL++))
    fi
}

assert_contains_multiline() {
    local file="$1"
    local pattern="$2"
    local description="$3"

    if perl -0777 -ne "exit(/$pattern/ ? 0 : 1)" "$file" 2>/dev/null; then
        echo "  PASS: $description"
        ((PASS++))
    else
        echo "  FAIL: $description"
        echo "        Expected multiline pattern in $file"
        ((FAIL++))
    fi
}

echo "=== SARIF Configuration Tests (Issue #10) ==="
echo ""

# --- Test 1: build.gradle.kts has sarif.required.set(true) ---
echo "Test 1: build.gradle.kts enables SARIF output"
assert_contains \
    "$PROJECT_ROOT/build.gradle.kts" \
    "sarif.required.set(true)" \
    "sarif.required.set(true) is present in build.gradle.kts"

# --- Test 2: CI workflow has security-events: write permission ---
echo "Test 2: CI workflow quality job has security-events: write permission"
assert_contains \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "security-events: write" \
    "security-events: write permission is present"

# --- Test 3: CI workflow uses --continue flag for detekt ---
echo "Test 3: CI workflow runs detekt with --continue flag"
assert_contains \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "gradlew detekt --continue" \
    "detekt runs with --continue flag"

# --- Test 4: CI workflow has upload-sarif step ---
echo "Test 4: CI workflow has upload-sarif step"
assert_contains \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "github/codeql-action/upload-sarif@v3" \
    "upload-sarif@v3 action is present"

# --- Test 5: upload-sarif step uses if: always() ---
echo "Test 5: upload-sarif step runs even on failure (if: always())"
assert_contains_multiline \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "Upload Detekt SARIF.*\n.*if: always\(\)" \
    "Upload Detekt SARIF step has if: always()"

# --- Test 6: Detekt step has continue-on-error: true ---
echo "Test 6: Detekt step has continue-on-error: true"
assert_contains \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "continue-on-error: true" \
    "Detekt step has continue-on-error: true"

# --- Test 7: upload-sarif has category: detekt ---
echo "Test 7: upload-sarif has category: detekt"
assert_contains \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "category: detekt" \
    "upload-sarif has category: detekt"

# --- Test 8: CI workflow quality job has contents: read permission ---
echo "Test 8: CI workflow quality job has contents: read permission"
assert_contains \
    "$PROJECT_ROOT/.github/workflows/ci.yml" \
    "contents: read" \
    "contents: read permission is present"

# --- Summary ---
echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
    exit 1
fi
