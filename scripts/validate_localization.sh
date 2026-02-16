#!/bin/bash
# chmod +x scripts/validate_localization.sh

# FoodShare Cross-Platform Localization Validation Script
# Verifies all hardcoded strings have been replaced with t.t() calls

echo "FoodShare Localization Validation (Cross-Platform)"
echo "========================================"

# Check for remaining hardcoded strings in critical files
echo "Scanning for remaining hardcoded strings..."

# Critical patterns that should be localized
PATTERNS=(
    "Text(\"[A-Z]"
    "Button(\"[A-Z]"
    "Label(\"[A-Z]"
    "navigationTitle(\"[A-Z]"
)

CRITICAL_FILES=(
    "Sources/FoodShare/App/MainTab/MainTabView.swift"
    "Sources/FoodShare/Features/Challenges/Presentation/Views/ChallengesView.swift"
    "Sources/FoodShare/Features/Forum/Presentation/Views/ForumPostDetailView.swift"
    "Sources/FoodShare/Features/Search/Presentation/Views/SearchView.swift"
    "Sources/FoodShare/Features/Messaging/Presentation/Views/MessagingView.swift"
    "Sources/FoodShare/Features/Listing/Presentation/Views/FoodItemDetailView.swift"
)

ISSUES_FOUND=0

for pattern in "${PATTERNS[@]}"; do
    echo "  Checking pattern: $pattern"

    for file in "${CRITICAL_FILES[@]}"; do
        if [ -f "$file" ]; then
            matches=$(grep -n "$pattern" "$file" 2>/dev/null || true)
            if [ ! -z "$matches" ]; then
                echo "    FAIL: Found in $file:"
                echo "$matches" | sed 's/^/      /'
                ISSUES_FOUND=$((ISSUES_FOUND + 1))
            fi
        fi
    done
done

# Check for proper t.t() usage
echo ""
echo "Verifying t.t() implementation..."

T_CALLS=$(find Sources/FoodShare -name "*.swift" -exec grep -l "t\.t(" {} \; 2>/dev/null | wc -l)
echo "  Found t.t() calls in $T_CALLS files"

# Check critical translation keys are used
CRITICAL_KEYS=(
    "tabs.explore"
    "tabs.chats"
    "tabs.challenges"
    "tabs.forum"
    "tabs.profile"
    "common.save"
    "common.cancel"
    "forum.discussion"
    "challenges.leaderboard"
)

echo ""
echo "Verifying critical translation keys..."

for key in "${CRITICAL_KEYS[@]}"; do
    usage=$(find Sources/FoodShare -name "*.swift" -exec grep -l "\"$key\"" {} \; 2>/dev/null | wc -l)
    if [ $usage -gt 0 ]; then
        echo "  OK: $key: Used in $usage file(s)"
    else
        echo "  MISSING: $key: Not found"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
done

# Summary
echo ""
echo "VALIDATION SUMMARY"
echo "===================="

if [ $ISSUES_FOUND -eq 0 ]; then
    echo "SUCCESS: All localization checks passed!"
    echo "   - No hardcoded strings found in critical files"
    echo "   - All critical translation keys implemented"
    echo "   - Ready for international deployment"
    exit 0
else
    echo "ISSUES FOUND: $ISSUES_FOUND problems detected"
    echo "   Please review and fix the issues above"
    exit 1
fi
