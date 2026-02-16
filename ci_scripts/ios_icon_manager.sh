#!/bin/bash
# chmod +x ci_scripts/ios_icon_manager.sh

# iOS App Icon Manager for FoodShare (Cross-Platform)
# Optimized for iOS App Store requirements and Xcode Cloud
# Handles all required iOS icon sizes with efficient batch processing
# Checks both Darwin/ and Sources/FoodShare/Resources/ asset catalogs

set -euo pipefail # Strict error handling

# iOS App Icon Configuration
# Check both possible locations for the icon asset catalog
DARWIN_ICON_DIR="Darwin/Assets.xcassets/AppIcon.appiconset"
SOURCES_ICON_DIR="Sources/FoodShare/Resources/Assets.xcassets/AppIcon.appiconset"

if [[ -n "${ICON_DIR:-}" ]]; then
    # User override
    :
elif [[ -d "$DARWIN_ICON_DIR" ]]; then
    ICON_DIR="$DARWIN_ICON_DIR"
elif [[ -d "$SOURCES_ICON_DIR" ]]; then
    ICON_DIR="$SOURCES_ICON_DIR"
else
    echo "ERROR: No AppIcon.appiconset found in either:"
    echo "  - $DARWIN_ICON_DIR"
    echo "  - $SOURCES_ICON_DIR"
    exit 1
fi

SOURCE_ICON="${ICON_DIR}/Icon-App-1024x1024@1x.png"

# iOS App Icon Sizes
readonly ICON_SIZE_IPHONE_2X=120
readonly ICON_SIZE_IPHONE_3X=180
readonly ICON_SIZE_IPAD_1X=76
readonly ICON_SIZE_IPAD_2X=152
readonly ICON_SIZE_SETTINGS_2X=58
readonly ICON_SIZE_SETTINGS_3X=87
readonly ICON_SIZE_SPOTLIGHT_2X=80
readonly ICON_SIZE_SPOTLIGHT_3X=120
readonly ICON_SIZE_NOTIFICATION_2X=40
readonly ICON_SIZE_NOTIFICATION_3X=60
readonly ICON_SIZE_APPSTORE=1024

# File size validation
readonly MIN_ICON_FILE_SIZE=1000  # bytes

# Colors for output
readonly COLOR_RED='\033[0;31m'
readonly COLOR_GREEN='\033[0;32m'
readonly COLOR_YELLOW='\033[1;33m'
readonly COLOR_BLUE='\033[0;34m'
readonly COLOR_RESET='\033[0m'

# Logging functions
log_info() {
    echo -e "${COLOR_BLUE}iOS Icons${COLOR_RESET} $1"
}

log_success() {
    echo -e "${COLOR_GREEN}OK${COLOR_RESET} $1"
}

log_warning() {
    echo -e "${COLOR_YELLOW}WARN${COLOR_RESET} $1"
}

log_error() {
    echo -e "${COLOR_RED}ERROR${COLOR_RESET} $1"
}

# Validate iOS icon source
validate_source_icon() {
    log_info "Validating iOS app icon source..."

    # Check sips availability (required for icon generation)
    if ! command -v sips >/dev/null 2>&1; then
        log_error "sips command not found - required for icon generation"
        log_error "This should be available on all macOS systems"
        return 1
    fi

    if [[ ! -f ${SOURCE_ICON} ]]; then
        log_error "Source 1024px icon not found: ${SOURCE_ICON}"
        log_error "Required for App Store submission"
        return 1
    fi

    # Check file size (should not be empty)
    local file_size
    file_size=$(stat -f%z "${SOURCE_ICON}" 2>/dev/null || stat -c%s "${SOURCE_ICON}" 2>/dev/null || echo "0")

    if [[ ${file_size} == "0" ]]; then
        log_error "Source icon is empty: ${SOURCE_ICON}"
        return 1
    fi

    # Validate it's a PNG file (iOS requirement)
    if command -v file >/dev/null 2>&1; then
        local file_type
        file_type=$(file "${SOURCE_ICON}")
        if [[ ${file_type} != *"PNG"* ]]; then
            log_error "Source icon must be PNG format for iOS"
            return 1
        fi
    fi

    # Validate dimensions using sips
    if command -v sips >/dev/null 2>&1; then
        local width height
        width=$(sips -g pixelWidth "${SOURCE_ICON}" 2>/dev/null | awk '/pixelWidth:/ {print $2}')
        height=$(sips -g pixelHeight "${SOURCE_ICON}" 2>/dev/null | awk '/pixelHeight:/ {print $2}')

        if [[ "${width}" != "1024" ]] || [[ "${height}" != "1024" ]]; then
            log_error "Source icon must be 1024x1024 (got ${width}x${height})"
            return 1
        fi
    fi

    # Check for alpha channel (App Store rejects icons with alpha)
    if command -v sips >/dev/null 2>&1; then
        local has_alpha
        has_alpha=$(sips -g hasAlpha "${SOURCE_ICON}" 2>/dev/null | awk '/hasAlpha:/ {print $2}')

        if [[ "${has_alpha}" == "yes" ]]; then
            log_error "Source icon has alpha channel - App Store WILL reject"
            log_error "Fix with: sips -s format png --deleteColorManagementProperties ${SOURCE_ICON}"
            log_error "Or remove alpha: convert ${SOURCE_ICON} -background white -alpha remove -alpha off ${SOURCE_ICON}"
            return 1
        fi
    fi

    log_success "Source icon validated (${file_size} bytes, 1024x1024, no alpha)"
    return 0
}

# Generate all iOS icons from source
generate_ios_icons() {
    log_info "Generating iOS app icons from 1024x1024 source..."

    if ! validate_source_icon; then
        return 1
    fi

    local generated_count=0

    # Generate all required sizes using sips
    log_info "Creating iOS icon sizes..."

    # iPhone icons
    sips -z $ICON_SIZE_IPHONE_2X $ICON_SIZE_IPHONE_2X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-60x60@2x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-60x60@2x.png"
    sips -z $ICON_SIZE_IPHONE_3X $ICON_SIZE_IPHONE_3X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-60x60@3x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-60x60@3x.png"

    # iPad icons
    sips -z $ICON_SIZE_IPAD_1X $ICON_SIZE_IPAD_1X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-76x76@1x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-76x76@1x.png"
    sips -z $ICON_SIZE_IPAD_2X $ICON_SIZE_IPAD_2X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-76x76@2x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-76x76@2x.png"

    # Spotlight icons
    sips -z 40 40 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-40x40@1x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-40x40@1x.png"
    sips -z $ICON_SIZE_SPOTLIGHT_2X $ICON_SIZE_SPOTLIGHT_2X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-40x40@2x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-40x40@2x.png"
    sips -z $ICON_SIZE_SPOTLIGHT_3X $ICON_SIZE_SPOTLIGHT_3X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-40x40@3x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-40x40@3x.png"

    # Settings icons
    sips -z 29 29 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@1x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-29x29@1x.png"
    sips -z $ICON_SIZE_SETTINGS_2X $ICON_SIZE_SETTINGS_2X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@2x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-29x29@2x.png"
    sips -z $ICON_SIZE_SETTINGS_3X $ICON_SIZE_SETTINGS_3X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-29x29@3x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-29x29@3x.png"

    # Notification icons
    sips -z 20 20 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-20x20@1x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-20x20@1x.png"
    sips -z $ICON_SIZE_NOTIFICATION_2X $ICON_SIZE_NOTIFICATION_2X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-20x20@2x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-20x20@2x.png"
    sips -z $ICON_SIZE_NOTIFICATION_3X $ICON_SIZE_NOTIFICATION_3X "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-20x20@3x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-20x20@3x.png"

    # iPad Pro 83.5@2x = 167
    sips -z 167 167 "${SOURCE_ICON}" --out "${ICON_DIR}/Icon-App-83.5x83.5@2x.png" >/dev/null 2>&1 && ((generated_count++)) || log_warning "Failed: Icon-App-83.5x83.5@2x.png"

    log_success "Generated ${generated_count} iOS icons"
}

# Validate all iOS icons for App Store compliance
validate_ios_icons() {
    log_info "Validating iOS icons for App Store compliance..."

    local missing_icons=()
    local invalid_icons=()
    local symlink_icons=()
    local total_valid=0

    # Critical icons to check
    local critical_icons=(
        "Icon-App-1024x1024@1x.png:1024:AppStore"
        "Icon-App-60x60@2x.png:120:iPhone"
        "Icon-App-60x60@3x.png:180:iPhone"
        "Icon-App-76x76@2x.png:152:iPad"
    )

    for icon_spec in "${critical_icons[@]}"; do
        IFS=':' read -r icon_name expected_size device <<< "${icon_spec}"
        local icon_path="${ICON_DIR}/${icon_name}"

        if [[ ! -f ${icon_path} ]]; then
            missing_icons+=("${icon_name} (${device}, ${expected_size}x${expected_size})")
            continue
        fi

        # CRITICAL: Check for symbolic links (causes App Store rejection)
        if [[ -L ${icon_path} ]]; then
            symlink_icons+=("${icon_name}")
            continue
        fi

        # Validate file has content
        local file_size
        file_size=$(stat -f%z "${icon_path}" 2>/dev/null || stat -c%s "${icon_path}" 2>/dev/null || echo "0")

        if [[ ${file_size} -lt ${MIN_ICON_FILE_SIZE} ]]; then
            invalid_icons+=("${icon_name} (${file_size} bytes, too small)")
            continue
        fi

        # Validate dimensions using sips
        if command -v sips >/dev/null 2>&1; then
            local width height
            width=$(sips -g pixelWidth "${icon_path}" 2>/dev/null | awk '/pixelWidth:/ {print $2}')
            height=$(sips -g pixelHeight "${icon_path}" 2>/dev/null | awk '/pixelHeight:/ {print $2}')

            if [[ "${width}" == "${expected_size}" ]] && [[ "${height}" == "${expected_size}" ]]; then
                log_success "${icon_name} (${device}): ${width}x${height}, ${file_size} bytes"
                ((total_valid++))
            else
                invalid_icons+=("${icon_name} (${width}x${height}, expected ${expected_size}x${expected_size})")
            fi
        else
            log_success "${icon_name} (${device}): ${file_size} bytes"
            ((total_valid++))
        fi
    done

    # Report validation results
    local has_errors=false

    if [[ ${#symlink_icons[@]} -gt 0 ]]; then
        log_error "CRITICAL: Found ${#symlink_icons[@]} symbolic links (App Store will reject):"
        printf '   %s\n' "${symlink_icons[@]}"
        has_errors=true
    fi

    if [[ ${#missing_icons[@]} -gt 0 ]]; then
        log_error "Missing ${#missing_icons[@]} critical icons:"
        printf '   %s\n' "${missing_icons[@]}"
        has_errors=true
    fi

    if [[ ${#invalid_icons[@]} -gt 0 ]]; then
        log_error "Invalid ${#invalid_icons[@]} icons:"
        printf '   %s\n' "${invalid_icons[@]}"
        has_errors=true
    fi

    if [[ "${has_errors}" == "true" ]]; then
        log_error "Icon validation FAILED"
        log_info "Run '$0 generate' to regenerate icons"
        return 1
    fi

    log_success "All ${total_valid} critical iOS icons validated"
    log_success "App Store submission requirements met"
    return 0
}

# Quick fix for critical iOS icons only
quick_ios_fix() {
    log_info "Quick iOS icon fix (critical icons only)..."

    if ! validate_source_icon; then
        return 1
    fi

    # Generate only the most critical icons for App Store
    local critical_sizes=(
        "Icon-App-60x60@2x.png:120"
        "Icon-App-60x60@3x.png:180"
        "Icon-App-76x76@2x.png:152"
    )

    for spec in "${critical_sizes[@]}"; do
        IFS=':' read -r filename size <<< "${spec}"
        local icon_path="${ICON_DIR}/${filename}"

        sips -z "${size}" "${size}" "${SOURCE_ICON}" --out "${icon_path}" 2>/dev/null || {
            log_error "Failed to generate critical icon: ${filename}"
            return 1
        }
        log_success "Generated ${filename} (${size}x${size})"
    done

    log_success "Critical iOS icons generated"
}

# Invalidate asset catalog cache
invalidate_cache() {
    log_info "Invalidating asset catalog cache..."

    # Touch Contents.json to force actool to reprocess
    if [[ -f "${ICON_DIR}/Contents.json" ]]; then
        touch "${ICON_DIR}/Contents.json"
        log_success "Touched ${ICON_DIR}/Contents.json"
    fi

    # Touch the appiconset directory itself
    if [[ -d "${ICON_DIR}" ]]; then
        touch "${ICON_DIR}"
        log_success "Touched ${ICON_DIR}/"
    fi

    # Touch the xcassets parent directory
    local xcassets_dir
    xcassets_dir=$(dirname "${ICON_DIR}")
    if [[ -d "${xcassets_dir}" ]]; then
        touch "${xcassets_dir}"
        log_success "Touched ${xcassets_dir}/"
    fi

    log_success "Asset catalog cache invalidated"
}

# Sync icons between Darwin/ and Sources/ asset catalogs
sync_icons() {
    log_info "Syncing icons between Darwin/ and Sources/ asset catalogs..."

    if [[ ! -d "$DARWIN_ICON_DIR" ]] || [[ ! -d "$SOURCES_ICON_DIR" ]]; then
        log_warning "Cannot sync - both directories must exist"
        log_warning "  Darwin:  $DARWIN_ICON_DIR ($([ -d "$DARWIN_ICON_DIR" ] && echo "exists" || echo "missing"))"
        log_warning "  Sources: $SOURCES_ICON_DIR ($([ -d "$SOURCES_ICON_DIR" ] && echo "exists" || echo "missing"))"
        return 1
    fi

    # Copy from current ICON_DIR to the other directory
    local target_dir
    if [[ "$ICON_DIR" == "$DARWIN_ICON_DIR" ]]; then
        target_dir="$SOURCES_ICON_DIR"
    else
        target_dir="$DARWIN_ICON_DIR"
    fi

    cp "${ICON_DIR}"/*.png "${target_dir}/" 2>/dev/null || true
    cp "${ICON_DIR}/Contents.json" "${target_dir}/Contents.json" 2>/dev/null || true

    log_success "Icons synced to ${target_dir}"
}

# Main execution function
main() {
    local action="${1:-generate}"

    # Ensure icon directory exists
    if [[ ! -d ${ICON_DIR} ]]; then
        log_error "iOS AppIcon.appiconset directory not found: ${ICON_DIR}"
        log_error "Please ensure Xcode project has proper app icon asset catalog"
        exit 1
    fi

    echo "Using icon directory: ${ICON_DIR}"

    case "${action}" in
    "generate")
        generate_ios_icons
        invalidate_cache
        ;;
    "validate")
        validate_ios_icons
        ;;
    "quick")
        quick_ios_fix
        invalidate_cache
        ;;
    "invalidate")
        invalidate_cache
        ;;
    "sync")
        sync_icons
        ;;
    "all")
        log_info "Complete iOS icon processing..."
        generate_ios_icons && invalidate_cache && validate_ios_icons
        ;;
    "help" | "--help" | "-h")
        echo "iOS Icon Manager for FoodShare (Cross-Platform)"
        echo ""
        echo "Usage: $0 [action]"
        echo ""
        echo "Actions:"
        echo "  generate    Generate all iOS app icons from 1024px source"
        echo "  validate    Validate existing iOS icons for App Store"
        echo "  quick       Quick fix for critical iOS icons only"
        echo "  invalidate  Invalidate asset catalog cache"
        echo "  sync        Sync icons between Darwin/ and Sources/ asset catalogs"
        echo "  all         Generate, invalidate cache, and validate (recommended)"
        echo "  help        Show this help message"
        echo ""
        echo "Icon directories checked:"
        echo "  - $DARWIN_ICON_DIR"
        echo "  - $SOURCES_ICON_DIR"
        echo ""
        echo "Requirements:"
        echo "  - Source icon: 1024x1024 PNG, no alpha"
        echo "  - Target: iOS 17+ App Store submission"
        exit 0
        ;;
    *)
        log_error "Unknown action: ${action}"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
    esac
}

# Execute with all provided arguments
main "$@"
