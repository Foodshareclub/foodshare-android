#!/bin/bash
# chmod +x scripts/summary.sh

# FoodShare Cross-Platform Project Summary

echo "FoodShare - Cross-Platform Project Summary"
echo ""
echo "======================================================"
echo "  Project Info"
echo "======================================================"
echo ""

# Read version info from Skip.env
if [ -f "Skip.env" ]; then
    VERSION=$(grep "^MARKETING_VERSION" Skip.env | sed 's/.*= *//')
    BUILD=$(grep "^CURRENT_PROJECT_VERSION" Skip.env | sed 's/.*= *//')
    BUNDLE_ID=$(grep "^PRODUCT_BUNDLE_IDENTIFIER" Skip.env | sed 's/.*= *//')
    echo "  Version:    $VERSION (build $BUILD)"
    echo "  Bundle ID:  $BUNDLE_ID"
else
    echo "  Skip.env not found"
fi

echo "  Framework:  Skip Fuse (Swift -> iOS + Android)"
echo "  Platforms:  iOS 17+ | Android 28+"
echo ""

echo "======================================================"
echo "  Code Metrics"
echo "======================================================"
echo ""

# Source code stats
SOURCES_LOC=$(find Sources/FoodShare -name "*.swift" -type f -exec wc -l {} \; 2>/dev/null | awk '{sum+=$1} END {print sum+0}')
SOURCES_FILES=$(find Sources/FoodShare -name "*.swift" -type f 2>/dev/null | wc -l | tr -d ' ')
TEST_LOC=$(find Tests -name "*.swift" -type f -exec wc -l {} \; 2>/dev/null | awk '{sum+=$1} END {print sum+0}')
TEST_FILES=$(find Tests -name "*.swift" -type f 2>/dev/null | wc -l | tr -d ' ')

echo "  Sources:     $SOURCES_LOC LOC ($SOURCES_FILES files)"
echo "  Tests:       $TEST_LOC LOC ($TEST_FILES files)"
echo "  Total:       $((SOURCES_LOC + TEST_LOC)) LOC"
echo ""

# Feature count
FEATURE_COUNT=$(ls -1d Sources/FoodShare/Features/*/ 2>/dev/null | wc -l | tr -d ' ')
echo "  Features:    $FEATURE_COUNT modules"
echo ""

echo "======================================================"
echo "  iOS Info"
echo "======================================================"
echo ""

if [ -d "Darwin/FoodShare.xcodeproj" ]; then
    echo "  Xcode Project: Darwin/FoodShare.xcodeproj"
    echo "  Assets:        Darwin/Assets.xcassets/"
    echo "  Info.plist:    Darwin/Info.plist"
    echo "  Entitlements:  Darwin/Entitlements.plist"
else
    echo "  Darwin/ directory not found"
fi
echo ""

echo "======================================================"
echo "  Android Info"
echo "======================================================"
echo ""

if [ -d "Android" ]; then
    echo "  Gradle Root:   Android/"
    if [ -f "Android/app/build.gradle.kts" ]; then
        echo "  App Module:    Android/app/build.gradle.kts"
    fi
    ANDROID_PKG=$(grep "^ANDROID_PACKAGE_NAME" Skip.env 2>/dev/null | sed 's/.*= *//')
    if [ -n "$ANDROID_PKG" ]; then
        echo "  Package:       $ANDROID_PKG"
    fi
else
    echo "  Android/ directory not found"
fi
echo ""

echo "======================================================"
echo "  Architecture"
echo "======================================================"
echo ""

echo "  Pattern:     Clean Architecture + MVVM + @Observable"
echo "  Data Layer:  API-first (Edge Functions via APIClient)"
echo "  Fallback:    Direct Supabase in catch blocks"
echo "  Design:      Liquid Glass design system"
echo ""

# Count design files
DESIGN_FILES=$(find Sources/FoodShare/Core/Design -name "*.swift" -type f 2>/dev/null | wc -l | tr -d ' ')
echo "  Design Files: $DESIGN_FILES"
echo ""

echo "======================================================"
echo "  Localization"
echo "======================================================"
echo ""

LOCALE_COUNT=$(ls -1d Sources/FoodShare/Resources/*.lproj 2>/dev/null | wc -l | tr -d ' ')
echo "  Locales: $LOCALE_COUNT"

for lproj in Sources/FoodShare/Resources/*.lproj; do
    if [ -d "$lproj" ]; then
        locale=$(basename "$lproj" .lproj)
        printf "    - %s\n" "$locale"
    fi
done
echo ""

echo "======================================================"
echo "  Available Scripts"
echo "======================================================"
echo ""
echo "  ./scripts/quick-start.sh               - Quick start guide"
echo "  ./scripts/summary.sh                   - This summary"
echo "  ./scripts/validate_localization.sh      - Check i18n coverage"
echo "  ./scripts/translate.sh                  - Translation management CLI"
echo "  ./scripts/generate-bridges.sh           - Generate Skip bridges"
echo "  ./scripts/run-cross-platform-tests.sh   - Cross-platform test runner"
echo ""
echo "  ./ci_scripts/bump-build.sh             - Increment build number"
echo "  ./ci_scripts/bump-version.sh           - Bump version (major|minor|patch)"
echo "  ./ci_scripts/generate-release-notes.sh - Generate release notes from git"
echo "  ./ci_scripts/ios_icon_manager.sh       - Manage iOS app icons"
echo "  ./ci_scripts/setup-monitoring.sh       - Production monitoring guide"
echo ""
