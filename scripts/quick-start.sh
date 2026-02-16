#!/bin/bash
# chmod +x scripts/quick-start.sh

# FoodShare Cross-Platform - Quick Start

set -e

echo "FoodShare - Cross-Platform Quick Start (Skip Fuse)"
echo ""
echo "Platforms: iOS 17+ | Android 28+"
echo "Framework: Skip Fuse 1.7.2 | Swift 6.1"
echo ""
echo "Project Structure:"
echo "  Sources/FoodShare/     - All Swift source code (shared)"
echo "  Darwin/                - iOS-specific (Xcode project)"
echo "  Android/               - Android-specific (Gradle)"
echo "  Tests/FoodShareTests/  - Unit tests"
echo ""
echo "Key Modules:"
echo "  Core/Design/           - Liquid Glass design system (137 files)"
echo "  Core/Networking/       - APIClient + Edge Function types"
echo "  Core/Services/         - Auth, location, analytics"
echo "  Features/              - 24 feature modules"
echo ""

# Build Swift package
echo "Building Swift package..."
swift build

echo ""
echo "Build complete!"
echo ""
echo "Next steps:"
echo ""
echo "  iOS Development:"
echo "    open Darwin/FoodShare.xcodeproj"
echo "    # Build and run via Xcode (FoodShare scheme)"
echo ""
echo "  Android Development:"
echo "    cd Android && ./gradlew assembleDebug"
echo "    cd Android && ./gradlew installDebug"
echo ""
echo "  Run Tests:"
echo "    swift test"
echo ""
echo "  Available Scripts:"
echo "    ./scripts/quick-start.sh               - This guide"
echo "    ./scripts/summary.sh                   - Project summary"
echo "    ./scripts/validate_localization.sh      - Check i18n coverage"
echo "    ./scripts/translate.sh                  - Translation management CLI"
echo "    ./scripts/generate-bridges.sh           - Generate Skip bridges"
echo "    ./scripts/run-cross-platform-tests.sh   - Cross-platform test runner"
echo ""
echo "  CI Scripts:"
echo "    ./ci_scripts/bump-build.sh             - Increment build number"
echo "    ./ci_scripts/bump-version.sh           - Bump version (major|minor|patch)"
echo "    ./ci_scripts/generate-release-notes.sh - Generate release notes"
echo "    ./ci_scripts/ios_icon_manager.sh       - Manage iOS app icons"
echo ""
