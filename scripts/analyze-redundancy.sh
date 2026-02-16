#!/bin/bash

# Analyze codebase for redundant code

echo "🔍 Analyzing Codebase for Redundancy"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Redundancy Report"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Count duplicate error enums
echo "1️⃣ Error Enums (LocalizedError)"
ERROR_COUNT=$(find Sources/FoodShare -name "*.swift" -type f -exec grep -l "enum.*Error.*LocalizedError" {} \; | wc -l | tr -d ' ')
echo "   Found: $ERROR_COUNT error enums"
echo "   💡 Consider: Consolidate into FoodShareCore/Errors"
echo ""

# Count cache implementations
echo "2️⃣ Cache Implementations"
CACHE_COUNT=$(find Sources/FoodShare -name "*.swift" -type f -exec grep -l "class.*Cache.*{" {} \; | wc -l | tr -d ' ')
echo "   Found: $CACHE_COUNT cache classes"
echo "   💡 Consider: Use FoodShareCache package"
echo ""

# Count Supabase client initializations
echo "3️⃣ Supabase Client Initializations"
SUPABASE_COUNT=$(find Sources/FoodShare -name "*.swift" -type f -exec grep -l "SupabaseClient(" {} \; | wc -l | tr -d ' ')
echo "   Found: $SUPABASE_COUNT direct initializations"
echo "   💡 Consider: Use SupabaseClientWrapper.shared"
echo ""

# Count analytics tracking implementations
echo "4️⃣ Analytics Implementations"
ANALYTICS_COUNT=$(find Sources/FoodShare -name "*.swift" -type f -exec grep -l "func track.*event.*properties" {} \; | wc -l | tr -d ' ')
echo "   Found: $ANALYTICS_COUNT analytics implementations"
echo "   💡 Consider: Use FoodShareAnalytics package"
echo ""

# Count LAContext usages
echo "5️⃣ Biometric Auth (LAContext)"
BIOMETRIC_COUNT=$(find Sources/FoodShare -name "*.swift" -type f -exec grep -l "LAContext()" {} \; | wc -l | tr -d ' ')
echo "   Found: $BIOMETRIC_COUNT LAContext usages"
echo "   💡 Consider: Use BiometricAuth from FoodShareSecurity"
echo ""

# Count Keychain direct access
echo "6️⃣ Keychain Direct Access"
KEYCHAIN_COUNT=$(find Sources/FoodShare -name "*.swift" -type f -exec grep -l "Keychain(service:" {} \; | wc -l | tr -d ' ')
echo "   Found: $KEYCHAIN_COUNT direct keychain accesses"
echo "   💡 Consider: Use SecureStorage from FoodShareSecurity"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📈 Potential Savings"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Calculate total lines in FoodShare directory
TOTAL_LINES=$(find Sources/FoodShare -name "*.swift" -type f -exec wc -l {} \; | awk '{sum+=$1} END {print sum}')
echo "   Total Swift LOC: $TOTAL_LINES"
echo ""

# Estimate redundancy
REDUNDANT_ERRORS=$((ERROR_COUNT * 30))  # ~30 lines per error enum
REDUNDANT_CACHE=$((CACHE_COUNT * 50))   # ~50 lines per cache
REDUNDANT_SUPABASE=$((SUPABASE_COUNT * 10))  # ~10 lines per init
REDUNDANT_ANALYTICS=$((ANALYTICS_COUNT * 40))  # ~40 lines per impl
REDUNDANT_BIOMETRIC=$((BIOMETRIC_COUNT * 20))  # ~20 lines per usage
REDUNDANT_KEYCHAIN=$((KEYCHAIN_COUNT * 15))  # ~15 lines per access

TOTAL_REDUNDANT=$((REDUNDANT_ERRORS + REDUNDANT_CACHE + REDUNDANT_SUPABASE + REDUNDANT_ANALYTICS + REDUNDANT_BIOMETRIC + REDUNDANT_KEYCHAIN))

echo "   Estimated redundant code: ~$TOTAL_REDUNDANT lines"
echo "   Potential reduction: ~$((TOTAL_REDUNDANT * 100 / TOTAL_LINES))%"
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎯 Recommended Actions"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  Priority 1 (High Impact):"
echo "    • Consolidate error enums into shared package"
echo "    • Replace all cache implementations with FoodShareCache"
echo "    • Use SupabaseClientWrapper everywhere"
echo ""
echo "  Priority 2 (Medium Impact):"
echo "    • Migrate to FoodShareAnalytics"
echo "    • Use BiometricAuth from FoodShareSecurity"
echo "    • Use SecureStorage from FoodShareSecurity"
echo ""
echo "  Priority 3 (Code Quality):"
echo "    • Remove duplicate utility functions"
echo "    • Consolidate similar view modifiers"
echo "    • Extract common validation logic"
echo ""
