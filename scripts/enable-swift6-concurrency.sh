#!/bin/bash

# Enable Swift 6 strict concurrency checking
# This is a bleeding-edge improvement that eliminates data races at compile time

set -e

echo "🚀 Enabling Swift 6 Strict Concurrency..."
echo ""

# Update Package.swift to enable strict concurrency
echo "📝 Updating Package.swift..."

# Add Swift 6 language mode and strict concurrency
cat > Package.swift.new << 'EOF'
// swift-tools-version: 6.0
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "Foodshare",
    platforms: [
        .iOS(.v17),
    ],
    products: [
        .library(
            name: "Foodshare",
            targets: ["Foodshare"],
        ),
    ],
    dependencies: [
        // Local packages
        .package(path: "Packages/FoodShareDesignSystem"),
        .package(path: "../foodshare-core"),
        
        // External dependencies
        .package(url: "https://github.com/supabase/supabase-swift.git", exact: "2.40.0"),
        .package(url: "https://github.com/airbnb/lottie-ios.git", from: "4.6.0"),
        .package(url: "https://github.com/onevcat/Kingfisher.git", from: "8.6.2"),
        .package(url: "https://github.com/siteline/swiftui-introspect.git", from: "26.0.0"),
        .package(url: "https://github.com/kishikawakatsumi/KeychainAccess.git", from: "4.2.2"),
        .package(url: "https://github.com/SwiftUIX/SwiftUIX.git", from: "0.2.3"),
        .package(url: "https://github.com/krzysztofzablocki/Inject.git", from: "1.5.2"),
        .package(url: "https://github.com/getsentry/sentry-cocoa.git", from: "9.2.0"),
    ],
    targets: [
        .target(
            name: "Foodshare",
            dependencies: [
                .product(name: "FoodShareDesignSystem", package: "FoodShareDesignSystem"),
                .product(name: "FoodshareCore", package: "foodshare-core"),
                .product(name: "Supabase", package: "supabase-swift"),
                .product(name: "Auth", package: "supabase-swift"),
                .product(name: "PostgREST", package: "supabase-swift"),
                .product(name: "Realtime", package: "supabase-swift"),
                .product(name: "Storage", package: "supabase-swift"),
                .product(name: "Lottie", package: "lottie-ios"),
                .product(name: "Kingfisher", package: "Kingfisher"),
                .product(name: "SwiftUIIntrospect", package: "swiftui-introspect"),
                .product(name: "KeychainAccess", package: "KeychainAccess"),
                .product(name: "SwiftUIX", package: "SwiftUIX"),
                .product(name: "Inject", package: "Inject"),
                .product(name: "Sentry", package: "sentry-cocoa"),
            ],
            path: "Sources/FoodShare",
            swiftSettings: [
                // Enable Swift 6 language mode
                .enableUpcomingFeature("StrictConcurrency"),
                .enableUpcomingFeature("BareSlashRegexLiterals"),
                .enableUpcomingFeature("ConciseMagicFile"),
                .enableUpcomingFeature("ForwardTrailingClosures"),
                .enableUpcomingFeature("ImplicitOpenExistentials"),
                .enableUpcomingFeature("DisableOutwardActorInference"),
                
                // Enable complete concurrency checking
                .unsafeFlags(["-Xfrontend", "-strict-concurrency=complete"]),
            ]
        ),
        .testTarget(
            name: "FoodshareTests",
            dependencies: ["Foodshare"],
            path: "Tests/FoodShareTests",
        ),
    ],
)
EOF

mv Package.swift.new Package.swift

echo "✅ Package.swift updated with Swift 6 concurrency settings"
echo ""
echo "⚠️  Next steps:"
echo "1. Build the project to see concurrency warnings"
echo "2. Fix warnings by:"
echo "   - Adding @Sendable to models"
echo "   - Using actors for shared mutable state"
echo "   - Adding @MainActor to UI code"
echo "   - Using async/await instead of callbacks"
echo ""
echo "3. Run: swift build 2>&1 | grep -i 'concurrency' | head -20"
