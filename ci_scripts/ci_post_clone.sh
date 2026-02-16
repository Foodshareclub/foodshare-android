#!/bin/bash
#
# XCODE CLOUD POST-CLONE SCRIPT FOR FOODSHARE (Cross-Platform / Skip Fuse)
# Version: 1.0.0
# Adapted from foodshare-ios ci_post_clone.sh v4.0.0 for Skip Fuse structure
#

echo "================================================================================"
echo "🥗 FOODSHARE (CROSS-PLATFORM) CI_POST_CLONE.SH IS RUNNING (v1.0.0)"
echo "================================================================================"
echo "Timestamp: $(date)"
echo "Script location: $0"
echo "Current directory: $(pwd)"
echo "Cache-Bust-ID: $(uuidgen)"
echo "================================================================================"

set -euo pipefail

# Navigate to repository root with robust path resolution
if [ -n "${CI_WORKSPACE:-}" ]; then
    cd "$CI_WORKSPACE"
    echo "📂 Using CI_WORKSPACE: $CI_WORKSPACE"
else
    # Fallback: use git to find repo root, or relative path
    REPO_ROOT=$(git rev-parse --show-toplevel 2>/dev/null || cd "$(dirname "$0")/.." && pwd)
    cd "$REPO_ROOT"
    echo "📂 Resolved repository root: $REPO_ROOT"
fi

echo "📂 Working directory: $(pwd)"
echo "🕒 Script execution time: $(date '+%Y-%m-%d %H:%M:%S')"

# =============================================================================
# SOURCE UTILITY LIBRARIES
# =============================================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -f "$SCRIPT_DIR/lib/logging.sh" ]; then
    source "$SCRIPT_DIR/lib/logging.sh"
    log_success "Loaded logging utilities"
else
    echo "⚠️  logging.sh not found, using basic logging"
    log_info() { echo "ℹ️  $*"; }
    log_success() { echo "✅ $*"; }
    log_warn() { echo "⚠️  $*"; }
    log_error() { echo "❌ $*"; }
    log_phase() { echo ""; echo "=== $* ==="; echo ""; }
    log_section() { echo ""; echo "--- $* ---"; }
fi

# =============================================================================
# CONSTANTS (Adapted for Skip Fuse cross-platform structure)
# =============================================================================

PROJECT_FILE="Darwin/FoodShare.xcodeproj/project.pbxproj"
SKIP_ENV="Skip.env"
ICON_DIR_DARWIN="Darwin/Assets.xcassets/AppIcon.appiconset"
ICON_DIR_SOURCES="Sources/FoodShare/Resources/Assets.xcassets/AppIcon.appiconset"
EXPECTED_VERSION_OCCURRENCES=4  # Debug/Release x Project/Target

# Determine which icon directory to use
if [ -d "$ICON_DIR_DARWIN" ]; then
    ICON_DIR="$ICON_DIR_DARWIN"
elif [ -d "$ICON_DIR_SOURCES" ]; then
    ICON_DIR="$ICON_DIR_SOURCES"
else
    ICON_DIR="$ICON_DIR_DARWIN"  # Default, will be created if needed
fi

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

# Create timestamped backup of project file
create_project_backup() {
    local suffix="${1:-general}"
    local backup_file="${PROJECT_FILE}.backup-${suffix}-$(date +%Y%m%d-%H%M%S)"

    log_info "Creating safety backup: $backup_file"
    if cp "$PROJECT_FILE" "$backup_file"; then
        echo "$backup_file"
        return 0
    else
        log_error "Failed to create backup"
        return 1
    fi
}

# Restore project file from backup
restore_project_backup() {
    local backup_file="$1"

    if [ -f "$backup_file" ]; then
        log_warn "Restoring backup: $backup_file"
        mv "$backup_file" "$PROJECT_FILE"
        return 0
    else
        log_error "Backup file not found: $backup_file"
        return 1
    fi
}

# Validate semantic version format (X.Y.Z)
validate_version_format() {
    local version="$1"

    if echo "$version" | grep -qE '^[0-9]+\.[0-9]+\.[0-9]+$'; then
        return 0
    else
        log_error "Invalid version format: '$version' (expected X.Y.Z)"
        return 1
    fi
}

# Validate build number format (integer)
validate_build_format() {
    local build="$1"

    if echo "$build" | grep -qE '^[0-9]+$'; then
        return 0
    else
        log_error "Invalid build number format: '$build' (expected integer)"
        return 1
    fi
}

# Update Skip.env value
update_skip_env() {
    local key="$1"
    local value="$2"

    if [ -f "$SKIP_ENV" ]; then
        if grep -q "^${key} = " "$SKIP_ENV"; then
            sed -i '' "s/^${key} = .*/${key} = ${value}/" "$SKIP_ENV"
            log_info "Updated $SKIP_ENV: $key = $value"
        else
            log_warn "$key not found in $SKIP_ENV"
        fi
    else
        log_warn "$SKIP_ENV not found"
    fi
}

# =============================================================================
# STEP -3: GENERATE ENCRYPTION COMPLIANCE DOCUMENTATION
# =============================================================================
log_phase "STEP -3: Generating Encryption Compliance Documentation"

if [ -f "$SCRIPT_DIR/lib/encryption_compliance.sh" ]; then
    source "$SCRIPT_DIR/lib/encryption_compliance.sh"

    # Generate documentation in repository docs folder
    generate_encryption_compliance_doc "docs/APP_STORE_ENCRYPTION_COMPLIANCE.md"

    log_success "Encryption compliance documentation ready for App Store submission"
else
    log_warn "encryption_compliance.sh not found, skipping"
fi

# =============================================================================
# STEP -2: AUTO-BUMP MARKETING VERSION (Patch Increment)
# =============================================================================
log_phase "STEP -2: Auto-Bumping Marketing Version (Patch Increment)"

# Debug: Check if xcodeproj exists
if [ -d "Darwin/FoodShare.xcodeproj" ]; then
    log_success "Darwin/FoodShare.xcodeproj directory found"
else
    log_error "Darwin/FoodShare.xcodeproj directory not found"
    find . -maxdepth 2 -name "*.xcodeproj" -type d
    exit 1
fi

if [ ! -f "$PROJECT_FILE" ]; then
    log_error "Project file not found: $PROJECT_FILE"
    log_info "Current directory: $(pwd)"
    log_info "CI_WORKSPACE: ${CI_WORKSPACE:-not set}"
    exit 1
fi

# Create backup before any modifications
BACKUP_FILE=$(create_project_backup "version") || exit 1

# Get current marketing version with validation
CURRENT_VERSION=$(grep "MARKETING_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*MARKETING_VERSION = \(.*\);/\1/' | tr -d ' ')

# If not found in project.pbxproj, try Skip.env
if [ -z "$CURRENT_VERSION" ] && [ -f "$SKIP_ENV" ]; then
    CURRENT_VERSION=$(grep "^MARKETING_VERSION = " "$SKIP_ENV" | sed 's/^MARKETING_VERSION = //' | tr -d ' ')
    log_info "Read version from Skip.env: $CURRENT_VERSION"
fi

# Validate version format
if ! validate_version_format "$CURRENT_VERSION"; then
    log_info "Expected format: X.Y.Z (e.g., 3.0.1)"
    restore_project_backup "$BACKUP_FILE"
    exit 1
fi

log_info "Current marketing version: $CURRENT_VERSION (format verified)"

# Parse version components
VERSION_MAJOR=$(echo "$CURRENT_VERSION" | cut -d. -f1)
VERSION_MINOR=$(echo "$CURRENT_VERSION" | cut -d. -f2)
VERSION_PATCH=$(echo "$CURRENT_VERSION" | cut -d. -f3)

# Validate component parsing
if [ -z "$VERSION_MAJOR" ] || [ -z "$VERSION_MINOR" ] || [ -z "$VERSION_PATCH" ]; then
    log_error "Failed to parse version components"
    log_info "Major: '$VERSION_MAJOR' Minor: '$VERSION_MINOR' Patch: '$VERSION_PATCH'"
    restore_project_backup "$BACKUP_FILE"
    exit 1
fi

# Increment patch version
VERSION_PATCH=$((VERSION_PATCH + 1))
NEW_VERSION="${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_PATCH}"

log_info "Auto-bumping version: $CURRENT_VERSION -> $NEW_VERSION"

# Update project file (Darwin/FoodShare.xcodeproj/project.pbxproj)
if ! sed -i '' "s/MARKETING_VERSION = $CURRENT_VERSION;/MARKETING_VERSION = $NEW_VERSION;/g" "$PROJECT_FILE"; then
    log_error "sed command failed on project.pbxproj"
    restore_project_backup "$BACKUP_FILE"
    exit 1
fi

# Multi-step verification for project file
VERIFY_VERSION=$(grep "MARKETING_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*MARKETING_VERSION = \(.*\);/\1/' | tr -d ' ')
VERSION_COUNT=$(grep -c "MARKETING_VERSION = $NEW_VERSION;" "$PROJECT_FILE")

if [ "$VERIFY_VERSION" = "$NEW_VERSION" ]; then
    log_success "Marketing version updated in project.pbxproj"
    log_info "Verified: $VERIFY_VERSION"
    log_info "Occurrences: $VERSION_COUNT (expected: ${EXPECTED_VERSION_OCCURRENCES})"

    # Cleanup backup on success
    rm -f "$BACKUP_FILE"
    log_info "Backup removed (update successful)"
else
    log_error "Version update verification failed in project.pbxproj"
    log_info "Expected: $NEW_VERSION, Got: $VERIFY_VERSION"
    restore_project_backup "$BACKUP_FILE"
    exit 1
fi

# Sync version to Skip.env (Android version sync)
log_section "Syncing version to Skip.env (Android)"
update_skip_env "MARKETING_VERSION" "$NEW_VERSION"

# Verify Skip.env update
if [ -f "$SKIP_ENV" ]; then
    SKIP_VERSION=$(grep "^MARKETING_VERSION = " "$SKIP_ENV" | sed 's/^MARKETING_VERSION = //' | tr -d ' ')
    if [ "$SKIP_VERSION" = "$NEW_VERSION" ]; then
        log_success "Skip.env version synced: $SKIP_VERSION"
    else
        log_warn "Skip.env version mismatch: expected $NEW_VERSION, got $SKIP_VERSION"
    fi
fi

# =============================================================================
# STEP -1: AUTO-INCREMENT BUILD NUMBER
# =============================================================================
log_phase "STEP -1: Auto-Incrementing Build Number"

# Create backup before build number modification
BACKUP_FILE_BUILD=$(create_project_backup "build") || exit 1

# Get current build number
CURRENT_BUILD=$(grep "CURRENT_PROJECT_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*CURRENT_PROJECT_VERSION = \(.*\);/\1/' | tr -d ' ')

# If not found in project.pbxproj, try Skip.env
if [ -z "$CURRENT_BUILD" ] && [ -f "$SKIP_ENV" ]; then
    CURRENT_BUILD=$(grep "^CURRENT_PROJECT_VERSION = " "$SKIP_ENV" | sed 's/^CURRENT_PROJECT_VERSION = //' | tr -d ' ')
    log_info "Read build number from Skip.env: $CURRENT_BUILD"
fi

# Validate build number format
if ! validate_build_format "$CURRENT_BUILD"; then
    log_info "Expected: Integer (e.g., 246)"
    restore_project_backup "$BACKUP_FILE_BUILD"
    exit 1
fi

log_info "Current build number: $CURRENT_BUILD (format verified)"

# Auto-increment build number
NEW_BUILD=$((CURRENT_BUILD + 1))

# Safety check
if [ "$NEW_BUILD" -le "$CURRENT_BUILD" ]; then
    log_error "Build increment failed"
    restore_project_backup "$BACKUP_FILE_BUILD"
    exit 1
fi

log_info "Auto-incrementing build: $CURRENT_BUILD -> $NEW_BUILD"

# Update project file
if ! sed -i '' "s/CURRENT_PROJECT_VERSION = $CURRENT_BUILD;/CURRENT_PROJECT_VERSION = $NEW_BUILD;/g" "$PROJECT_FILE"; then
    log_error "sed command failed"
    restore_project_backup "$BACKUP_FILE_BUILD"
    exit 1
fi

# Verify
VERIFY_BUILD=$(grep "CURRENT_PROJECT_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*CURRENT_PROJECT_VERSION = \(.*\);/\1/' | tr -d ' ')
BUILD_COUNT=$(grep -c "CURRENT_PROJECT_VERSION = $NEW_BUILD;" "$PROJECT_FILE")

if [ "$VERIFY_BUILD" = "$NEW_BUILD" ]; then
    log_success "Build number updated in project.pbxproj"
    log_info "Verified: $VERIFY_BUILD"
    log_info "Occurrences: $BUILD_COUNT"
    log_info "This build: Version $NEW_VERSION (Build $NEW_BUILD)"

    rm -f "$BACKUP_FILE_BUILD"
else
    log_error "Build update verification failed"
    restore_project_backup "$BACKUP_FILE_BUILD"
    exit 1
fi

# Sync build number to Skip.env (Android version sync)
log_section "Syncing build number to Skip.env (Android)"
update_skip_env "CURRENT_PROJECT_VERSION" "$NEW_BUILD"

# Verify Skip.env update
if [ -f "$SKIP_ENV" ]; then
    SKIP_BUILD=$(grep "^CURRENT_PROJECT_VERSION = " "$SKIP_ENV" | sed 's/^CURRENT_PROJECT_VERSION = //' | tr -d ' ')
    if [ "$SKIP_BUILD" = "$NEW_BUILD" ]; then
        log_success "Skip.env build number synced: $SKIP_BUILD"
    else
        log_warn "Skip.env build number mismatch: expected $NEW_BUILD, got $SKIP_BUILD"
    fi
fi

# =============================================================================
# STEP 0: ICON GENERATION AND VALIDATION
# =============================================================================
log_phase "STEP 0: Icon Generation and Validation"

if [[ ! -d "${ICON_DIR}" ]]; then
    log_warn "Icon directory not found: ${ICON_DIR}"
    log_info "Trying alternate icon locations..."

    # Try Darwin path
    if [[ -d "$ICON_DIR_DARWIN" ]]; then
        ICON_DIR="$ICON_DIR_DARWIN"
        log_success "Found icons at: $ICON_DIR_DARWIN"
    elif [[ -d "$ICON_DIR_SOURCES" ]]; then
        ICON_DIR="$ICON_DIR_SOURCES"
        log_success "Found icons at: $ICON_DIR_SOURCES"
    else
        log_error "No icon directory found at either location"
        log_info "  Checked: $ICON_DIR_DARWIN"
        log_info "  Checked: $ICON_DIR_SOURCES"
        exit 1
    fi
fi

log_success "Icon directory found: ${ICON_DIR}"

# Use ios_icon_manager.sh if available
if [ -f "$SCRIPT_DIR/ios_icon_manager.sh" ]; then
    log_info "Using ios_icon_manager.sh for icon management..."

    # Run icon generation and validation
    if "$SCRIPT_DIR/ios_icon_manager.sh" all; then
        log_success "Icon management completed via ios_icon_manager.sh"
    else
        log_warn "ios_icon_manager.sh reported issues, falling back to inline generation"
        # Fallback to inline generation
        SOURCE_ICON="${ICON_DIR}/Icon-App-1024x1024@1x.png"
        if [[ -f "${SOURCE_ICON}" ]]; then
            log_info "Generating iOS icon sizes from 1024x1024 source..."
            sips -z 120 120 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-60x60@2x.png" >/dev/null 2>&1 || true
            sips -z 152 152 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-76x76@2x.png" >/dev/null 2>&1 || true
            sips -z 180 180 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-60x60@3x.png" >/dev/null 2>&1 || true
            sips -z 80 80 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-40x40@2x.png" >/dev/null 2>&1 || true
            sips -z 87 87 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@3x.png" >/dev/null 2>&1 || true
            sips -z 58 58 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@2x.png" >/dev/null 2>&1 || true
            log_success "Icon generation completed (fallback)"
        fi
    fi
else
    # Inline icon generation (legacy fallback)
    SOURCE_ICON="${ICON_DIR}/Icon-App-1024x1024@1x.png"

    if [[ -f "${SOURCE_ICON}" ]]; then
        log_info "Generating iOS icon sizes from 1024x1024 source..."

        sips -z 120 120 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-60x60@2x.png" >/dev/null 2>&1 || true
        sips -z 152 152 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-76x76@2x.png" >/dev/null 2>&1 || true
        sips -z 180 180 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-60x60@3x.png" >/dev/null 2>&1 || true
        sips -z 80 80 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-40x40@2x.png" >/dev/null 2>&1 || true
        sips -z 87 87 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@3x.png" >/dev/null 2>&1 || true
        sips -z 58 58 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@2x.png" >/dev/null 2>&1 || true

        log_success "Icon generation completed"
    else
        log_warn "Source icon not found: ${SOURCE_ICON}"
    fi
fi

# Check for symbolic links (critical - causes App Store rejection)
log_section "Checking for symbolic links (App Store rejection cause)"
SYMLINK_COUNT=$(find "${ICON_DIR}" -type l 2>/dev/null | wc -l | tr -d ' ')
if [[ ${SYMLINK_COUNT} -gt 0 ]]; then
    log_error "Found ${SYMLINK_COUNT} symbolic links in app icons!"
    log_error "This will cause App Store Connect rejection"
    find "${ICON_DIR}" -type l -exec ls -la {} \;
    exit 1
fi
log_success "No symbolic links found"

# Force asset catalog cache invalidation
log_section "Asset Catalog Cache Invalidation"
touch "${ICON_DIR}/Contents.json"
touch "${ICON_DIR}"
# Invalidate both possible asset catalog locations
if [ -d "Darwin/Assets.xcassets" ]; then
    touch "Darwin/Assets.xcassets"
fi
if [ -d "Sources/FoodShare/Resources/Assets.xcassets" ]; then
    touch "Sources/FoodShare/Resources/Assets.xcassets"
fi
log_success "Asset catalog cache invalidated"

# =============================================================================
# STEP 0.5: SECRETS INJECTION FROM CI ENVIRONMENT
# =============================================================================
log_phase "STEP 0.5: Secrets Injection"

CONFIG_PLIST_PATH="Sources/FoodShare/Resources/Config.plist"

# Check if secrets are provided via environment variables
if [ -n "${SUPABASE_URL:-}" ] && [ -n "${SUPABASE_PUBLISHABLE_KEY:-}" ]; then
    log_info "Injecting secrets from CI environment variables..."

    # Ensure the directory exists
    mkdir -p "$(dirname "$CONFIG_PLIST_PATH")"

    # Create or update Config.plist with secrets
    cat > "$CONFIG_PLIST_PATH" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>SupabaseURL</key>
    <string>${SUPABASE_URL}</string>
    <key>SupabasePublishableKey</key>
    <string>${SUPABASE_PUBLISHABLE_KEY}</string>
    <key>BuildEnvironment</key>
    <string>CI_BUILD</string>
EOF

    # Add optional secrets if available
    if [ -n "${UPSTASH_REDIS_URL:-}" ]; then
        cat >> "$CONFIG_PLIST_PATH" <<EOF
    <key>UpstashRedisURL</key>
    <string>${UPSTASH_REDIS_URL}</string>
EOF
    fi

    if [ -n "${UPSTASH_REDIS_TOKEN:-}" ]; then
        cat >> "$CONFIG_PLIST_PATH" <<EOF
    <key>UpstashRedisToken</key>
    <string>${UPSTASH_REDIS_TOKEN}</string>
EOF
    fi

    if [ -n "${RESEND_API_KEY:-}" ]; then
        cat >> "$CONFIG_PLIST_PATH" <<EOF
    <key>ResendAPIKey</key>
    <string>${RESEND_API_KEY}</string>
EOF
    fi

    if [ -n "${NEXTDOOR_CLIENT_ID:-}" ]; then
        cat >> "$CONFIG_PLIST_PATH" <<EOF
    <key>NextdoorClientId</key>
    <string>${NEXTDOOR_CLIENT_ID}</string>
EOF
    fi

    # Close the plist
    cat >> "$CONFIG_PLIST_PATH" <<EOF
</dict>
</plist>
EOF

    log_success "Config.plist created with CI secrets"
    log_info "Secrets injected: SUPABASE_URL, SUPABASE_PUBLISHABLE_KEY + optional keys"
else
    log_info "No CI environment secrets detected, using existing Config.plist"

    if [ -f "$CONFIG_PLIST_PATH" ]; then
        log_success "Existing Config.plist found"
    else
        log_warn "Config.plist not found - build may fail without Supabase configuration"
    fi
fi

# =============================================================================
# STEP 1: XCODE CLOUD CONFIGURATION
# =============================================================================
log_phase "STEP 1: Xcode Cloud Configuration"

if [ ! -f "XcodeCloud.xcconfig" ]; then
    log_info "Creating XcodeCloud.xcconfig..."

    cat > "XcodeCloud.xcconfig" <<'EOF'
// XcodeCloud.xcconfig - FoodShare Cross-Platform (Skip Fuse)
// Auto-generated by ci_post_clone.sh

OTHER_SWIFT_FLAGS = -disable-cmo -whole-module-optimization -num-threads 1
SWIFT_COMPILATION_MODE = wholemodule
SWIFT_ENABLE_EXPLICIT_MODULES = NO
SWIFT_NUM_THREADS = 1
COMPILER_INDEX_STORE_ENABLE = NO
SWIFT_ENABLE_MODULE_VERIFIER = NO
EOF
    log_success "Created XcodeCloud.xcconfig"
else
    log_success "XcodeCloud.xcconfig exists"
fi

# Show the flags
log_info "Xcconfig content (Swift flags):"
grep -E "OTHER_SWIFT_FLAGS|SWIFT_COMPILATION_MODE|SWIFT_NUM_THREADS" XcodeCloud.xcconfig | grep -v "^//" || echo "  (no flags found)"

# =============================================================================
# STEP 2: INJECT COMPILER FLAGS INTO PROJECT
# =============================================================================
log_phase "STEP 2: Injecting Compiler Flags into project.pbxproj"

cp "$PROJECT_FILE" "${PROJECT_FILE}.backup"

# Remove existing OTHER_SWIFT_FLAGS
sed -i '' '/OTHER_SWIFT_FLAGS/d' "$PROJECT_FILE"

# Inject new flags after SWIFT_VERSION
sed -i '' '/SWIFT_VERSION = /a\
				OTHER_SWIFT_FLAGS = "-disable-cmo -whole-module-optimization -num-threads 1";
' "$PROJECT_FILE"

if grep -q "OTHER_SWIFT_FLAGS.*disable-cmo" "$PROJECT_FILE"; then
    log_success "Compiler flags injected successfully"
    log_info "Flags: -disable-cmo -whole-module-optimization -num-threads 1"

    # Count injection locations
    INJECTION_COUNT=$(grep -c "OTHER_SWIFT_FLAGS.*disable-cmo" "$PROJECT_FILE" || echo "0")
    log_info "Injected into $INJECTION_COUNT configurations"
else
    log_error "Flag injection failed!"
    exit 1
fi

# =============================================================================
# FINAL: POST-CLONE COMPLETE
# =============================================================================
log_phase "POST-CLONE SETUP COMPLETE"

echo ""
echo "Summary of Changes:"
echo "   STEP -3: Encryption compliance documentation generated"
echo "   STEP -2: Marketing version auto-bumped to $NEW_VERSION"
echo "            -> Updated Darwin/FoodShare.xcodeproj/project.pbxproj"
echo "            -> Synced to Skip.env (Android)"
echo "   STEP -1: Build number auto-incremented to $NEW_BUILD"
echo "            -> Updated Darwin/FoodShare.xcodeproj/project.pbxproj"
echo "            -> Synced to Skip.env (Android)"
echo "   STEP 0:  Icons generated, validated, and cache invalidated"
echo "   STEP 0.5: Secrets injected from CI environment (if available)"
echo "   STEP 1:  XcodeCloud.xcconfig verified/created"
echo "   STEP 2:  Compiler flags injected into project.pbxproj"
echo ""
echo "This build: FoodShare v$NEW_VERSION (Build $NEW_BUILD)"
echo ""
echo "Build should succeed!"
echo "================================================================================"
