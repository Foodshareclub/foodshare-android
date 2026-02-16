#!/bin/bash
# chmod +x ci_scripts/generate-release-notes.sh

# FoodShare Cross-Platform - Generate Release Notes from git commits since last tag

set -euo pipefail

# Get the last tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")

if [ -z "$LAST_TAG" ]; then
    echo "No previous tag found. Generating notes from all commits..."
    COMMITS=$(git log --pretty=format:"* %s" --no-merges)
else
    echo "Generating notes since $LAST_TAG..."
    COMMITS=$(git log $LAST_TAG..HEAD --pretty=format:"* %s" --no-merges)
fi

# Get current version from Skip.env (single source of truth for cross-platform)
VERSION=$(grep "^MARKETING_VERSION" Skip.env | sed 's/.*= *//')

# Generate release notes
cat > RELEASE_NOTES.txt << EOF
What's New in Version $VERSION

$COMMITS

Bug fixes and performance improvements
EOF

echo "Release notes generated in RELEASE_NOTES.txt"
cat RELEASE_NOTES.txt
