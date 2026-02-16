#!/bin/bash
# chmod +x ci_scripts/bump-version.sh

# FoodShare Cross-Platform - Bump Marketing Version
# Updates both Darwin/FoodShare.xcodeproj/project.pbxproj and Skip.env MARKETING_VERSION

set -euo pipefail

PROJECT_FILE="Darwin/FoodShare.xcodeproj/project.pbxproj"
SKIP_ENV="Skip.env"

# Get current marketing version from project file
CURRENT_VERSION=$(grep -m 1 "MARKETING_VERSION = " "$PROJECT_FILE" | sed 's/.*= \(.*\);/\1/')

# Parse version components
MAJOR=$(echo $CURRENT_VERSION | cut -d. -f1)
MINOR=$(echo $CURRENT_VERSION | cut -d. -f2)
PATCH=$(echo $CURRENT_VERSION | cut -d. -f3)

# Determine bump type (default: patch)
BUMP_TYPE="${1:-patch}"

case "$BUMP_TYPE" in
    major)
        MAJOR=$((MAJOR + 1))
        MINOR=0
        PATCH=0
        ;;
    minor)
        MINOR=$((MINOR + 1))
        PATCH=0
        ;;
    patch)
        PATCH=$((PATCH + 1))
        ;;
    *)
        echo "Usage: $0 [major|minor|patch]"
        exit 1
        ;;
esac

NEW_VERSION="$MAJOR.$MINOR.$PATCH"

# Update project file (all occurrences)
sed -i '' "s/MARKETING_VERSION = $CURRENT_VERSION;/MARKETING_VERSION = $NEW_VERSION;/g" "$PROJECT_FILE"

# Update Skip.env
sed -i '' "s/^MARKETING_VERSION = .*/MARKETING_VERSION = $NEW_VERSION/" "$SKIP_ENV"

echo "Version updated: $CURRENT_VERSION -> $NEW_VERSION ($BUMP_TYPE)"
echo "  Updated: $PROJECT_FILE"
echo "  Updated: $SKIP_ENV"

# Commit and push
git add "$PROJECT_FILE" "$SKIP_ENV"
git commit -m "Bump version to $NEW_VERSION"
git push

echo "Changes pushed to remote"
