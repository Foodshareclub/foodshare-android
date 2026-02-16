#!/bin/bash
#
# Smart Version Bumper for FoodShare (Cross-Platform)
# Auto-determines next valid version and build number
# Updates both the Xcode project and Skip.env for cross-platform consistency.
#
# Usage:
#   ./ci_scripts/utils/bump_version_smart.sh [patch|minor|major]
#

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Navigate to project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$PROJECT_ROOT"

PROJECT_FILE="Darwin/FoodShare.xcodeproj/project.pbxproj"
SKIP_ENV_FILE="Skip.env"

if [ ! -f "$PROJECT_FILE" ]; then
    echo -e "${RED}Error: Cannot find $PROJECT_FILE${NC}"
    exit 1
fi

if [ ! -f "$SKIP_ENV_FILE" ]; then
    echo -e "${YELLOW}Warning: Cannot find $SKIP_ENV_FILE — will only update Xcode project${NC}"
fi

# Get current marketing version
get_current_marketing_version() {
    grep "MARKETING_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*MARKETING_VERSION = \(.*\);/\1/' | tr -d ' '
}

# Get current build number
get_current_build_number() {
    grep "CURRENT_PROJECT_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*CURRENT_PROJECT_VERSION = \(.*\);/\1/' | tr -d ' '
}

# Increment version
increment_version() {
    local version=$1
    local bump_type=$2

    IFS='.' read -r -a parts <<< "$version"
    local major="${parts[0]}"
    local minor="${parts[1]}"
    local patch="${parts[2]}"

    case "$bump_type" in
        major)
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        minor)
            minor=$((minor + 1))
            patch=0
            ;;
        patch)
            patch=$((patch + 1))
            ;;
        *)
            echo -e "${RED}Invalid bump type: $bump_type${NC}"
            echo "Use: patch, minor, or major"
            exit 1
            ;;
    esac

    echo "$major.$minor.$patch"
}

# Update version in Xcode project
update_version_in_project() {
    local new_version=$1
    sed -i '' "s/MARKETING_VERSION = .*;/MARKETING_VERSION = $new_version;/g" "$PROJECT_FILE"
}

# Update build number in Xcode project
update_build_in_project() {
    local new_build=$1
    sed -i '' "s/CURRENT_PROJECT_VERSION = .*;/CURRENT_PROJECT_VERSION = $new_build;/g" "$PROJECT_FILE"
}

# Update version in Skip.env
update_skip_env() {
    local new_version=$1
    local new_build=$2

    if [ -f "$SKIP_ENV_FILE" ]; then
        sed -i '' "s/^MARKETING_VERSION = .*/MARKETING_VERSION = $new_version/" "$SKIP_ENV_FILE"
        sed -i '' "s/^CURRENT_PROJECT_VERSION = .*/CURRENT_PROJECT_VERSION = $new_build/" "$SKIP_ENV_FILE"
    fi
}

echo -e "${BLUE}Smart Version Bumper for FoodShare (Cross-Platform)${NC}\n"

CURRENT_VERSION=$(get_current_marketing_version)
CURRENT_BUILD=$(get_current_build_number)

echo -e "Current State:"
echo -e "   Version: ${YELLOW}$CURRENT_VERSION${NC}"
echo -e "   Build:   ${YELLOW}$CURRENT_BUILD${NC}\n"

BUMP_TYPE="${1:-patch}"

NEW_VERSION=$(increment_version "$CURRENT_VERSION" "$BUMP_TYPE")
NEW_BUILD=$((CURRENT_BUILD + 1))

echo -e "${GREEN}Proposed Changes:${NC}"
echo -e "   Version: ${YELLOW}$CURRENT_VERSION${NC} -> ${GREEN}$NEW_VERSION${NC} ($BUMP_TYPE)"
echo -e "   Build:   ${YELLOW}$CURRENT_BUILD${NC} -> ${GREEN}$NEW_BUILD${NC}\n"

read -p "Continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${RED}Aborted${NC}"
    exit 1
fi

echo -e "\n${BLUE}Applying changes...${NC}"

# Update Xcode project
update_version_in_project "$NEW_VERSION"
update_build_in_project "$NEW_BUILD"

# Update Skip.env for cross-platform consistency
update_skip_env "$NEW_VERSION" "$NEW_BUILD"

VERIFY_VERSION=$(get_current_marketing_version)
VERIFY_BUILD=$(get_current_build_number)

if [ "$VERIFY_VERSION" = "$NEW_VERSION" ] && [ "$VERIFY_BUILD" = "$NEW_BUILD" ]; then
    echo -e "${GREEN}Success!${NC}"
    echo -e "   Version: ${GREEN}$VERIFY_VERSION${NC}"
    echo -e "   Build:   ${GREEN}$VERIFY_BUILD${NC}"

    if [ -f "$SKIP_ENV_FILE" ]; then
        SKIP_VERSION=$(grep "^MARKETING_VERSION" "$SKIP_ENV_FILE" | sed 's/.*= //')
        SKIP_BUILD=$(grep "^CURRENT_PROJECT_VERSION" "$SKIP_ENV_FILE" | sed 's/.*= //')
        echo -e "   Skip.env: ${GREEN}$SKIP_VERSION (build $SKIP_BUILD)${NC}"
    fi
    echo ""

    GIT_TAG="v$NEW_VERSION-$NEW_BUILD"
    echo -e "${BLUE}Suggested next steps:${NC}"
    echo -e "   1. ${YELLOW}git add . && git commit -m 'chore: bump to v$NEW_VERSION (build $NEW_BUILD)'${NC}"
    echo -e "   2. ${YELLOW}git tag $GIT_TAG && git push origin $GIT_TAG${NC}"
    echo -e "   3. ${YELLOW}git push${NC}"
else
    echo -e "${RED}Verification failed${NC}"
    exit 1
fi
