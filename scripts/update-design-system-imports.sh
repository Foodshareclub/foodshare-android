#!/bin/bash

# Script to automatically add FoodShareDesignSystem imports where needed
# This completes Phase 1 of componentization

set -e

echo "🔍 Finding files that need FoodShareDesignSystem import..."

# Find all Swift files that use design system components but don't import the package
find Sources/FoodShare -name "*.swift" -type f | while read -r file; do
    # Check if file uses design system components
    if grep -qE "(LiquidGlass|GlassCard|GlassButton|PrimaryButton|SecondaryButton|\.DesignSystem\.|Theme\.|Spacing\.)" "$file"; then
        # Check if it already has the import
        if ! grep -q "import FoodShareDesignSystem" "$file"; then
            echo "  ✏️  Adding import to: $file"
            
            # Add import after the last import statement
            awk '/^import / { imports = imports $0 "\n"; next } 
                 !printed && imports { print imports "import FoodShareDesignSystem"; printed = 1 } 
                 { print }' "$file" > "$file.tmp" && mv "$file.tmp" "$file"
        fi
    fi
done

echo "✅ Import updates complete!"
echo ""
echo "Next steps:"
echo "1. Build the project: swift build"
echo "2. Run tests: swift test"
echo "3. Open in Xcode and verify"
