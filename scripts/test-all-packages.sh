#!/bin/bash

# Test all packages in the modular architecture

set -e

echo "🧪 Testing FoodShare iOS Packages"
echo ""

PACKAGES=(
    "FoodShareDesignSystem"
    "FoodShareNetworking"
    "FoodShareSecurity"
    "FoodShareFeatureFlags"
    "FoodShareAnalytics"
    "FoodShareCache"
    "FoodSharePerformance"
    "FoodShareRouter"
)

FAILED=0
PASSED=0

for package in "${PACKAGES[@]}"; do
    echo "Testing $package..."
    
    if swift test --package-path "Packages/$package" 2>&1 | grep -q "Test Suite.*passed"; then
        echo "  ✅ $package tests passed"
        ((PASSED++))
    else
        echo "  ❌ $package tests failed"
        ((FAILED++))
    fi
    echo ""
done

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Test Results:"
echo "  ✅ Passed: $PASSED"
echo "  ❌ Failed: $FAILED"
echo "  📦 Total:  ${#PACKAGES[@]}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ $FAILED -eq 0 ]; then
    echo ""
    echo "🎉 All package tests passed!"
    exit 0
else
    echo ""
    echo "⚠️  Some tests failed. Review the output above."
    exit 1
fi
