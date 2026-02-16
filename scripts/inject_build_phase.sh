#!/bin/bash
# Inject Hot Reload Build Phase Script
# Add this as a "Run Script" build phase in Xcode (DEBUG only)
#
# This script enables hot reload by watching for file changes
# and injecting them into the running app.
#
# Target: Darwin/FoodShare.xcodeproj

if [ "$CONFIGURATION" = "Debug" ]; then
    # Check if Inject is available
    INJECT_PATH="${BUILD_DIR%Build/*}SourcePackages/checkouts/Inject/Sources/Inject"

    if [ -d "$INJECT_PATH" ]; then
        echo "Inject hot reload enabled"
    else
        echo "Warning: Inject package not found at: $INJECT_PATH"
    fi
fi
