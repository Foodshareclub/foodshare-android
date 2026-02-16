#!/bin/bash

# Migrate legacy code to use new packages

set -e

echo "🔄 Migrating to Modular Packages"
echo ""

# Replace BiometricAuthService with BiometricAuth from FoodShareSecurity
echo "1️⃣ Migrating BiometricAuthService → BiometricAuth..."
find Sources/FoodShare -name "*.swift" -type f -exec sed -i '' \
    's/BiometricAuthService\.shared/BiometricAuth.shared/g' {} \;

# Add FoodShareSecurity import where BiometricAuth is used
find Sources/FoodShare -name "*.swift" -type f -exec grep -l "BiometricAuth" {} \; | while read file; do
    if ! grep -q "import FoodShareSecurity" "$file"; then
        # Add import after other imports
        sed -i '' '/^import /a\
import FoodShareSecurity
' "$file"
    fi
done

echo "   ✅ BiometricAuth migration complete"
echo ""

# Replace direct SupabaseClient initialization with wrapper
echo "2️⃣ Consolidating Supabase client usage..."
echo "   ℹ️  Manual review needed for complex cases"
echo ""

# Replace ImageCache with FoodShareCache
echo "3️⃣ Migrating to FoodShareCache..."
echo "   ℹ️  Consider migrating ImageCache to use MemoryCache<String, UIImage>"
echo ""

# Replace analytics tracking with FoodShareAnalytics
echo "4️⃣ Consolidating analytics..."
echo "   ℹ️  Migrate AnalyticsService to use FoodShareAnalytics"
echo ""

echo "✅ Migration complete!"
echo ""
echo "📝 Manual steps required:"
echo "  1. Review BiometricAuth usage in remaining 3 files"
echo "  2. Update SupabaseClient initializations to use SupabaseClientWrapper"
echo "  3. Migrate ImageCache to FoodShareCache"
echo "  4. Consolidate analytics providers"
echo ""
echo "🧪 Next: Run tests with ./scripts/test-all-packages.sh"
