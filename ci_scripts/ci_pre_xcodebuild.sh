#!/bin/bash
#
# XCODE CLOUD PRE-BUILD SCRIPT FOR FOODSHARE (Cross-Platform / Skip Fuse)
# Version: 1.0.0
# Adapted from foodshare-ios ci_pre_xcodebuild.sh v4.0.0 for Skip Fuse structure
#

echo "================================================================================"
echo "🥗 FOODSHARE (CROSS-PLATFORM) CI_PRE_XCODEBUILD.SH IS RUNNING (v1.0.0)"
echo "================================================================================"
echo "Timestamp: $(date)"
echo "Script location: $0"
echo "Current directory: $(pwd)"
echo "================================================================================"

set -euo pipefail

cd "$(dirname "$0")/.."
echo "📂 Working directory: $(pwd)"

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
INFO_PLIST="Darwin/Info.plist"
CONFIG_PLIST="Sources/FoodShare/Resources/Config.plist"
SKIP_ENV="Skip.env"
ICON_DIR_DARWIN="Darwin/Assets.xcassets/AppIcon.appiconset"
ICON_DIR_SOURCES="Sources/FoodShare/Resources/Assets.xcassets/AppIcon.appiconset"

# Determine which icon directory to use
if [ -d "$ICON_DIR_DARWIN" ]; then
    ICON_DIR="$ICON_DIR_DARWIN"
elif [ -d "$ICON_DIR_SOURCES" ]; then
    ICON_DIR="$ICON_DIR_SOURCES"
else
    ICON_DIR="$ICON_DIR_DARWIN"  # Default
fi

# =============================================================================
# ENVIRONMENT VARIABLE VALIDATION
# =============================================================================
log_phase "Environment Configuration Validation"

required_env_vars=(
    "SUPABASE_URL"
    "SUPABASE_PUBLISHABLE_KEY"
)

missing_vars=()
for var in "${required_env_vars[@]}"; do
    if [[ -z ${!var-} ]]; then
        missing_vars+=("${var}")
    else
        # Redact sensitive values - show only first 8 chars
        log_success "${var}: ${!var:0:8}***"
    fi
done

if [[ ${#missing_vars[@]} -ne 0 ]]; then
    log_error "Missing required environment variables:"
    printf '   %s\n' "${missing_vars[@]}"
    echo ""
    echo "Configure in App Store Connect -> Xcode Cloud -> Environment Variables"
    exit 1
fi

log_success "All required environment variables present"

# =============================================================================
# CREATE CONFIGURATION FILES
# =============================================================================
log_phase "Creating Configuration Files"

# Ensure the directory exists
mkdir -p "$(dirname "$CONFIG_PLIST")"

# Generate Config.plist from environment variables
cat >"${CONFIG_PLIST}" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>SupabaseURL</key>
	<string>${SUPABASE_URL}</string>
	<key>SupabasePublishableKey</key>
	<string>${SUPABASE_PUBLISHABLE_KEY}</string>
	<key>BuildEnvironment</key>
	<string>XCODE_CLOUD</string>
	<key>BuildNumber</key>
	<string>${CI_BUILD_NUMBER:-1}</string>
</dict>
</plist>
EOF

log_success "Config.plist generated from Xcode Cloud environment variables"
log_info "Location: ${CONFIG_PLIST}"
log_info "SupabaseURL: ${SUPABASE_URL:0:30}..."
log_info "SupabasePublishableKey: ${SUPABASE_PUBLISHABLE_KEY:0:20}..."

# Validate Config.plist format
if plutil -lint "${CONFIG_PLIST}" >/dev/null 2>&1; then
    log_success "Config.plist format valid"
else
    log_error "Config.plist format invalid"
    plutil -lint "${CONFIG_PLIST}"
    exit 1
fi

# =============================================================================
# PROJECT VALIDATION
# =============================================================================
log_phase "Cross-Platform Project Validation"

validation_errors=0

if [[ ! -f "${PROJECT_FILE}" ]]; then
    log_error "Project file missing: ${PROJECT_FILE}"
    ((validation_errors++))
fi

if [[ ! -f "${INFO_PLIST}" ]]; then
    log_error "Info.plist missing: ${INFO_PLIST}"
    ((validation_errors++))
fi

if [[ ! -f "${SKIP_ENV}" ]]; then
    log_warn "Skip.env missing: ${SKIP_ENV}"
    log_info "Android builds may not have correct version numbers"
fi

if [[ ! -d "${ICON_DIR}" ]]; then
    log_warn "AppIcon.appiconset missing: ${ICON_DIR}"
    log_info "Will need icons before App Store submission"
fi

if [[ ! -f "Package.swift" ]]; then
    log_error "Package.swift missing - Skip Fuse project requires SPM manifest"
    ((validation_errors++))
fi

if [[ ${validation_errors} -gt 0 ]]; then
    log_error "Project validation failed (${validation_errors} errors)"
    exit 1
fi

log_success "Project validation passed"

# =============================================================================
# UPDATE BUILD NUMBER
# =============================================================================
log_phase "Build Number Update"

if [[ -n ${CI_BUILD_NUMBER-} ]]; then
    log_info "Using Xcode Cloud build number: ${CI_BUILD_NUMBER}"

    # Update via agvtool (operates on the Darwin xcodeproj)
    cd Darwin
    agvtool new-version -all "${CI_BUILD_NUMBER}"
    UPDATED_VERSION=$(agvtool what-version -terse)
    cd ..

    log_success "Build number updated to: ${UPDATED_VERSION}"

    # Also sync to Skip.env for Android consistency
    if [ -f "$SKIP_ENV" ]; then
        if grep -q "^CURRENT_PROJECT_VERSION = " "$SKIP_ENV"; then
            sed -i '' "s/^CURRENT_PROJECT_VERSION = .*/CURRENT_PROJECT_VERSION = ${CI_BUILD_NUMBER}/" "$SKIP_ENV"
            log_success "Skip.env build number synced to: ${CI_BUILD_NUMBER}"
        fi
    fi
else
    log_info "Not running in CI - build number unchanged"
fi

# =============================================================================
# ICON VALIDATION
# =============================================================================
log_phase "iOS App Icon Validation"

if [[ -d "${ICON_DIR}" ]]; then
    # Use ios_icon_manager.sh for validation if available
    if [ -f "$SCRIPT_DIR/ios_icon_manager.sh" ]; then
        log_info "Running icon validation via ios_icon_manager.sh..."

        if "$SCRIPT_DIR/ios_icon_manager.sh" validate; then
            log_success "Icon validation passed"
        else
            log_warn "Icon validation reported issues"
            log_info "Attempting icon regeneration..."
            "$SCRIPT_DIR/ios_icon_manager.sh" quick || true
        fi
    else
        # Fallback to inline validation
        log_section "Checking for symbolic links"

        # Check for symbolic links (causes App Store rejection)
        SYMLINK_COUNT=$(find "${ICON_DIR}" -type l 2>/dev/null | wc -l | tr -d ' ')
        if [[ ${SYMLINK_COUNT} -gt 0 ]]; then
            log_error "Found ${SYMLINK_COUNT} symbolic links in app icons"
            log_error "This will cause App Store Connect rejection"
            find "${ICON_DIR}" -type l -exec ls -la {} \;
            exit 1
        fi
        log_success "No symbolic links found"

        # Verify critical icons
        log_section "Verifying critical icons"
        for critical_icon in "Icon-App-60x60@2x.png" "Icon-App-76x76@2x.png" "Icon-App-1024x1024@1x.png"; do
            icon_path="${ICON_DIR}/${critical_icon}"
            if [[ -f "${icon_path}" ]]; then
                file_size=$(stat -f%z "${icon_path}" 2>/dev/null || echo "0")
                if [[ "${file_size}" == "0" ]]; then
                    log_error "${critical_icon}: EMPTY (0 bytes)"
                else
                    log_success "${critical_icon}: ${file_size} bytes"
                fi
            else
                log_warn "${critical_icon}: MISSING"
            fi
        done
    fi

    # Force cache invalidation
    touch "${ICON_DIR}/Contents.json"
    log_success "Asset catalog cache invalidated"
fi

# =============================================================================
# COMPILER FLAGS VERIFICATION
# =============================================================================
log_phase "Compiler Flags Verification"

if [[ -f "XcodeCloud.xcconfig" ]]; then
    log_success "XcodeCloud.xcconfig exists"
    log_info "Compiler flags:"
    grep -E "OTHER_SWIFT_FLAGS|SWIFT_COMPILATION_MODE" "XcodeCloud.xcconfig" | grep -v "^//" || echo "  (no flags found)"

    if grep -q "baseConfigurationReference.*XcodeCloud.xcconfig" "${PROJECT_FILE}"; then
        log_success "XcodeCloud.xcconfig linked to project"
    else
        log_warn "XcodeCloud.xcconfig not linked (may still work via injection)"
    fi
else
    log_warn "XcodeCloud.xcconfig not found"
fi

# Verify flags are injected in project
if grep -q "OTHER_SWIFT_FLAGS.*disable-cmo" "${PROJECT_FILE}"; then
    INJECTION_COUNT=$(grep -c "OTHER_SWIFT_FLAGS.*disable-cmo" "${PROJECT_FILE}" || echo "0")
    log_success "Compiler flags found in project ($INJECTION_COUNT configurations)"
else
    log_warn "Compiler flags not found in project.pbxproj"
    log_info "ci_post_clone.sh should have injected them"
fi

# =============================================================================
# BUILD CONFIGURATION
# =============================================================================
log_phase "Build Configuration"

if [[ ${CI_BRANCH-} =~ ^(main|master|release/.*)$ ]]; then
    export XCODE_CONFIGURATION="Release"
    log_info "Release configuration for production branch"
else
    export XCODE_CONFIGURATION="Debug"
    log_info "Debug configuration for development branch"
fi

# =============================================================================
# PRIVACY MANIFEST VALIDATION
# =============================================================================
log_phase "Privacy Manifest Validation"

# Check multiple possible locations for PrivacyInfo.xcprivacy
PRIVACY_MANIFEST=""
for possible_path in "PrivacyInfo.xcprivacy" "Darwin/PrivacyInfo.xcprivacy" "Sources/FoodShare/Resources/PrivacyInfo.xcprivacy"; do
    if [[ -f "${possible_path}" ]]; then
        PRIVACY_MANIFEST="${possible_path}"
        break
    fi
done

if [[ -n "${PRIVACY_MANIFEST}" ]]; then
    log_success "PrivacyInfo.xcprivacy found at: ${PRIVACY_MANIFEST}"

    # Validate plist format
    if plutil -lint "${PRIVACY_MANIFEST}" >/dev/null 2>&1; then
        log_success "Privacy manifest format valid"
    else
        log_error "Privacy manifest format invalid"
        plutil -lint "${PRIVACY_MANIFEST}"
        exit 1
    fi
else
    log_warn "PrivacyInfo.xcprivacy not found"
    log_info "Checked: PrivacyInfo.xcprivacy, Darwin/PrivacyInfo.xcprivacy, Sources/FoodShare/Resources/PrivacyInfo.xcprivacy"
    log_info "Required for App Store submission"
fi

# =============================================================================
# PRIVACY KEY VALIDATION
# =============================================================================
log_phase "Privacy Key Validation"

if [[ -f "${INFO_PLIST}" ]]; then
    required_privacy_keys=(
        "NSLocationWhenInUseUsageDescription"
        "NSLocationAlwaysAndWhenInUseUsageDescription"
        "NSCameraUsageDescription"
        "NSPhotoLibraryUsageDescription"
    )

    privacy_keys_valid=true
    for key in "${required_privacy_keys[@]}"; do
        if /usr/libexec/PlistBuddy -c "Print :$key" "$INFO_PLIST" >/dev/null 2>&1; then
            log_success "$key: Present"
        else
            log_error "Missing required privacy key: $key"
            privacy_keys_valid=false
        fi
    done

    if [[ "$privacy_keys_valid" != "true" ]]; then
        log_error "Privacy key validation failed - App Store will reject"
        log_info "Add missing keys to Darwin/Info.plist"
        exit 1
    fi

    log_success "All required privacy keys present"
else
    log_warn "Info.plist not found at ${INFO_PLIST}, skipping privacy key validation"
fi

# =============================================================================
# BUILD ENVIRONMENT SUMMARY
# =============================================================================
log_phase "Build Environment Summary"

# Get version info from project file
MARKETING_VERSION=$(grep "MARKETING_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*MARKETING_VERSION = \(.*\);/\1/' | tr -d ' ')
BUILD_NUMBER=$(grep "CURRENT_PROJECT_VERSION = " "$PROJECT_FILE" | head -1 | sed 's/.*CURRENT_PROJECT_VERSION = \(.*\);/\1/' | tr -d ' ')

# Fallback to Skip.env if not found in project
if [ -z "$MARKETING_VERSION" ] && [ -f "$SKIP_ENV" ]; then
    MARKETING_VERSION=$(grep "^MARKETING_VERSION = " "$SKIP_ENV" | sed 's/^MARKETING_VERSION = //' | tr -d ' ')
fi
if [ -z "$BUILD_NUMBER" ] && [ -f "$SKIP_ENV" ]; then
    BUILD_NUMBER=$(grep "^CURRENT_PROJECT_VERSION = " "$SKIP_ENV" | sed 's/^CURRENT_PROJECT_VERSION = //' | tr -d ' ')
fi

# Get Skip.env version for comparison
SKIP_VERSION=""
SKIP_BUILD=""
if [ -f "$SKIP_ENV" ]; then
    SKIP_VERSION=$(grep "^MARKETING_VERSION = " "$SKIP_ENV" | sed 's/^MARKETING_VERSION = //' | tr -d ' ')
    SKIP_BUILD=$(grep "^CURRENT_PROJECT_VERSION = " "$SKIP_ENV" | sed 's/^CURRENT_PROJECT_VERSION = //' | tr -d ' ')
fi

echo ""
echo "Build Environment Summary:"
echo "   App: FoodShare v${MARKETING_VERSION:-unknown} (Build ${BUILD_NUMBER:-unknown})"
echo "   Platform: Cross-Platform (Skip Fuse - iOS + Android)"
echo "   Target: iOS 17+ with SwiftUI & Supabase"
echo "   Branch: ${CI_BRANCH:-unknown} (${XCODE_CONFIGURATION})"
echo "   CI Build: ${CI_BUILD_NUMBER:-local}"
echo "   Xcode: ${CI_XCODE_VERSION:-unknown}"
echo ""
if [ -n "$SKIP_VERSION" ]; then
    echo "   Skip.env: v${SKIP_VERSION} (Build ${SKIP_BUILD:-?})"
    if [ "$SKIP_VERSION" != "${MARKETING_VERSION:-}" ] || [ "$SKIP_BUILD" != "${BUILD_NUMBER:-}" ]; then
        log_warn "Version mismatch between project.pbxproj and Skip.env!"
    else
        log_success "Versions in sync: project.pbxproj <-> Skip.env"
    fi
fi
echo ""
log_success "FoodShare cross-platform pre-build setup completed!"
echo "Ready for Xcode build"
echo "================================================================================"
