#!/bin/bash
# chmod +x ci_scripts/bump-build.sh

# FoodShare Cross-Platform - Bump Build Number
# Updates both Darwin/FoodShare.xcodeproj/project.pbxproj and Skip.env

set -euo pipefail

PROJECT_FILE="Darwin/FoodShare.xcodeproj/project.pbxproj"
SKIP_ENV="Skip.env"

# Get current build number from project file
CURRENT_BUILD=$(grep -m 1 "CURRENT_PROJECT_VERSION = " "$PROJECT_FILE" | sed 's/.*= \([0-9]*\);/\1/')

# Increment build number
NEW_BUILD=$((CURRENT_BUILD + 1))

# Update project file (all occurrences)
sed -i '' "s/CURRENT_PROJECT_VERSION = $CURRENT_BUILD;/CURRENT_PROJECT_VERSION = $NEW_BUILD;/g" "$PROJECT_FILE"

# Update Skip.env
sed -i '' "s/^CURRENT_PROJECT_VERSION = .*/CURRENT_PROJECT_VERSION = $NEW_BUILD/" "$SKIP_ENV"

echo "Build number updated: $CURRENT_BUILD -> $NEW_BUILD"
echo "  Updated: $PROJECT_FILE"
echo "  Updated: $SKIP_ENV"

# Commit and push
git add "$PROJECT_FILE" "$SKIP_ENV"
git commit -m "Bump build number to $NEW_BUILD"
git push

echo "Changes pushed to remote"
