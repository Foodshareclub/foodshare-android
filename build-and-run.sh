#!/bin/bash
set -e

# Build and Run Script for FoodShare Cross-Platform App
# Supports both iOS (Xcode) and Android (Gradle)

echo "🚀 FoodShare Cross-Platform Build Script"
echo "=========================================="
echo ""

# Determine platform
if [[ "$1" == "android" ]]; then
    PLATFORM="android"
elif [[ "$1" == "ios" ]]; then
    PLATFORM="ios"
else
    echo "Usage: $0 [ios|android] [options]"
    echo ""
    echo "iOS Options:"
    echo "  $0 ios                     # Build for iOS simulator"
    echo "  $0 ios --device            # Build for connected device"
    echo "  $0 ios --release           # Build release configuration"
    echo ""
    echo "Android Options:"
    echo "  $0 android                 # Build debug APK"
    echo "  $0 android --release       # Build release APK"
    exit 1
fi

# Build for iOS
if [[ "$PLATFORM" == "ios" ]]; then
    echo "📱 Building for iOS..."
    echo ""

    # Verify Swift version
    echo "📦 Checking Swift version..."
    SWIFT_VERSION=$(swift --version | head -n 1)
    echo "   $SWIFT_VERSION"
    echo ""

    # Clean build artifacts
    echo "🧹 Cleaning build artifacts..."
    rm -rf .build
    rm -rf ~/Library/Developer/Xcode/DerivedData/FoodShare-* 2>/dev/null || true
    echo ""

    # Check for device or simulator
    if [[ "$2" == "--device" ]]; then
        echo "📱 Building for connected device..."
        xcodebuild -project Darwin/FoodShare.xcodeproj \
            -scheme FoodShare \
            -configuration Debug \
            build
    else
        echo "📱 Available simulators:"
        xcrun simctl list devices available | grep iPhone | head -n 5
        echo ""

        echo "🔨 Building for iOS Simulator..."
        echo "   Project: Darwin/FoodShare.xcodeproj"
        echo "   Scheme: FoodShare"
        echo "   Destination: iPhone 15 Pro"
        echo ""

        xcodebuild -project Darwin/FoodShare.xcodeproj \
            -scheme FoodShare \
            -configuration Debug \
            -destination 'platform=iOS Simulator,name=iPhone 15 Pro' \
            -derivedDataPath .build/DerivedData \
            build
    fi

    echo ""
    echo "✅ iOS build completed successfully!"
    echo ""

# Build for Android
elif [[ "$PLATFORM" == "android" ]]; then
    echo "🤖 Building for Android..."
    echo ""

    if [[ ! -d "Android" ]]; then
        echo "❌ Error: Android/ directory not found"
        exit 1
    fi

    cd Android

    # Determine build variant
    if [[ "$2" == "--release" ]]; then
        echo "🔨 Building release APK..."
        ./gradlew assembleRelease
    else
        echo "🔨 Building debug APK..."
        ./gradlew assembleDebug
    fi

    echo ""
    echo "✅ Android build completed successfully!"
    echo ""

    cd ..
fi

echo "🎉 Build complete!"
