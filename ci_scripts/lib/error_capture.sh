#!/bin/bash
# chmod +x ci_scripts/lib/error_capture.sh

# lib/error_capture.sh
# Comprehensive error capture from xcodebuild and Swift compiler
#
# Captures, parses, and categorizes build errors for actionable diagnostics

# Source logging utilities
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/logging.sh"

# Parse xcodebuild output and extract structured errors
parse_xcodebuild_errors() {
    local build_log="$1"
    local error_summary_file="$2"

    log_section "Error Extraction from Build Log"

    if [ ! -f "$build_log" ]; then
        log_error "Build log not found: $build_log"
        return 1
    fi

    # Initialize error summary
    cat > "$error_summary_file" << EOF
# Build Error Summary
Generated: $(date)
Build Log: $build_log

## Error Breakdown by Category

EOF

    local total_errors=0
    local total_warnings=0

    # Extract errors by pattern
    log_info "Scanning for Swift compiler errors..."

    # Pattern 1: Type resolution errors (batch compilation issue)
    local type_errors
    type_errors=$(grep -c "error: cannot find type .* in scope" "$build_log" 2>/dev/null || echo "0")
    if [ "$type_errors" -gt 0 ]; then
        log_error "Found $type_errors type resolution errors"
        total_errors=$((total_errors + type_errors))

        echo "### Type Resolution Errors: $type_errors" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        grep "error: cannot find type .* in scope" "$build_log" | while read -r line; do
            echo "- $line" >> "$error_summary_file"
        done
        echo "" >> "$error_summary_file"

        # Extract file locations
        echo "**Affected Files:**" >> "$error_summary_file"
        grep -B 2 "error: cannot find type .* in scope" "$build_log" | \
            grep "\.swift:" | \
            sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Pattern 2: Missing imports
    local import_errors
    import_errors=$(grep -c "error:.*import.*not found\|error:.*module.*not found" "$build_log" 2>/dev/null || echo "0")
    if [ "$import_errors" -gt 0 ]; then
        log_error "Found $import_errors import errors"
        total_errors=$((total_errors + import_errors))

        echo "### Import Errors: $import_errors" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        grep "error:.*import.*not found\|error:.*module.*not found" "$build_log" | \
            sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Pattern 3: Swift concurrency errors
    local concurrency_errors
    concurrency_errors=$(grep -c "error:.*@MainActor\|error:.*Sendable\|error:.*actor-isolated" "$build_log" 2>/dev/null || echo "0")
    if [ "$concurrency_errors" -gt 0 ]; then
        log_error "Found $concurrency_errors concurrency errors"
        total_errors=$((total_errors + concurrency_errors))

        echo "### Swift Concurrency Errors: $concurrency_errors" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        grep "error:.*@MainActor\|error:.*Sendable\|error:.*actor-isolated" "$build_log" | \
            sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Pattern 4: Linker errors
    local linker_errors
    linker_errors=$(grep -c "Undefined symbols\|ld: symbol(s) not found" "$build_log" 2>/dev/null || echo "0")
    if [ "$linker_errors" -gt 0 ]; then
        log_error "Found $linker_errors linker errors"
        total_errors=$((total_errors + linker_errors))

        echo "### Linker Errors: $linker_errors" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        grep -A 5 "Undefined symbols\|ld: symbol(s) not found" "$build_log" | \
            sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Pattern 5: Code signing errors
    local signing_errors
    signing_errors=$(grep -c "error:.*code signing\|error:.*provisioning profile" "$build_log" 2>/dev/null || echo "0")
    if [ "$signing_errors" -gt 0 ]; then
        log_error "Found $signing_errors code signing errors"
        total_errors=$((total_errors + signing_errors))

        echo "### Code Signing Errors: $signing_errors" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        grep "error:.*code signing\|error:.*provisioning profile" "$build_log" | \
            sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Pattern 6: Asset Catalog compilation errors
    local asset_errors
    asset_errors=$(grep -c "error:.*Asset catalog\|error:.*actool\|error:.*AppIcon" "$build_log" 2>/dev/null || echo "0")
    if [ "$asset_errors" -gt 0 ]; then
        log_error "Found $asset_errors Asset Catalog errors"
        total_errors=$((total_errors + asset_errors))

        cat >> "$error_summary_file" << 'EOF'

### Asset Catalog Errors
======================================

Asset compilation failed (icons, images, or other assets).

COMMON CAUSES:
  - App icon has alpha channel (App Store rejects this)
  - Missing required icon sizes (1024x1024, 60x60@2x, etc.)
  - Corrupt image files
  - Invalid Contents.json in .appiconset or .imageset

FIX:
  1. Run: ./ci_scripts/ios_icon_manager.sh all
  2. Verify all icons are opaque (no alpha): sips -g hasAlpha Icon-App-1024x1024@1x.png
  3. Check Darwin/Assets.xcassets/AppIcon.appiconset/Contents.json
  4. Ensure all referenced icon files exist

======================================
EOF
    fi

    # Pattern 7: Info.plist errors
    local plist_errors
    plist_errors=$(grep -c "error:.*Info.plist\|error:.*property list" "$build_log" 2>/dev/null || echo "0")
    if [ "$plist_errors" -gt 0 ]; then
        log_error "Found $plist_errors Info.plist errors"
        total_errors=$((total_errors + plist_errors))

        cat >> "$error_summary_file" << 'EOF'

### Info.plist Configuration Errors
====================================

Info.plist is malformed or missing required keys.

COMMON CAUSES:
  - Malformed XML in Info.plist
  - Missing required privacy keys (NSLocationWhenInUseUsageDescription, etc.)
  - Invalid bundle identifier
  - Missing version strings

FIX:
  1. Validate plist: plutil -lint Darwin/Info.plist
  2. Check required keys exist:
     - CFBundleIdentifier
     - NSLocationWhenInUseUsageDescription
     - NSCameraUsageDescription
     - NSPhotoLibraryUsageDescription
  3. Fix any XML syntax errors

====================================
EOF
    fi

    # Pattern 8: Privacy manifest errors
    local privacy_errors
    privacy_errors=$(grep -c "error:.*PrivacyInfo\|error:.*privacy manifest" "$build_log" 2>/dev/null || echo "0")
    if [ "$privacy_errors" -gt 0 ]; then
        log_error "Found $privacy_errors privacy manifest errors"
        total_errors=$((total_errors + privacy_errors))

        cat >> "$error_summary_file" << 'EOF'

### Privacy Manifest Errors
========================================

Privacy manifest (PrivacyInfo.xcprivacy) is missing or invalid.

REQUIRED FOR APP STORE:
  - NSPrivacyTracking declaration
  - NSPrivacyCollectedDataTypes
  - NSPrivacyAccessedAPITypes with valid reasons

FIX:
  1. Ensure PrivacyInfo.xcprivacy exists at repository root
  2. Validate format: plutil -lint PrivacyInfo.xcprivacy
  3. Ensure it's added to Xcode project target

========================================
EOF
    fi

    # Pattern 9: Memory/resource exhaustion
    local memory_errors
    memory_errors=$(grep -c "error:.*out of memory\|error:.*resource temporarily unavailable\|Killed: 9" "$build_log" 2>/dev/null || echo "0")
    if [ "$memory_errors" -gt 0 ]; then
        log_critical "Found $memory_errors memory/resource errors"
        total_errors=$((total_errors + memory_errors))

        cat >> "$error_summary_file" << 'EOF'

### Memory/Resource Exhaustion Errors
======================================

Swift compiler ran out of memory during compilation.

ROOT CAUSE:
  Whole-module optimization compiles ALL files as single unit, requiring more memory.

WORKAROUNDS:
  Option A (Temporary): Reduce parallelism
    - Change: OTHER_SWIFT_FLAGS = -whole-module-optimization -num-threads 1

  Option B (Code): Split large files
    - Identify largest Swift files (>1000 lines)
    - Split into smaller modules or files

======================================
EOF
    fi

    # Pattern 10: Swift version mismatch
    local swift_version_errors
    swift_version_errors=$(grep -c "error:.*requires Swift.*or later\|error:.*compiled with Swift.*but" "$build_log" 2>/dev/null || echo "0")
    if [ "$swift_version_errors" -gt 0 ]; then
        log_error "Found $swift_version_errors Swift version errors"
        total_errors=$((total_errors + swift_version_errors))

        cat >> "$error_summary_file" << 'EOF'

### Swift Version Mismatch
===========================

Dependency requires different Swift version than project.

FOODSHARE REQUIREMENTS:
  - Project: Swift 6.1+
  - Deployment: iOS 17.0+ / Android 28+

COMMON CAUSES:
  - Dependency compiled with different Swift version
  - Xcode Cloud using wrong Xcode version
  - Package.swift specifies incompatible Swift version

FIX:
  1. Verify Xcode version in App Store Connect
  2. Check Package.swift swift-tools-version
  3. Update incompatible dependencies

===========================
EOF
    fi

    # Extract all other errors
    local other_errors
    other_errors=$(grep -c "^error:" "$build_log" 2>/dev/null || echo "0")
    if [ "$other_errors" -gt 0 ]; then
        log_warn "Found $other_errors additional errors"

        echo "### Other Errors: $other_errors" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        grep "^error:" "$build_log" | head -20 | sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Extract warnings
    total_warnings=$(grep -c "^warning:" "$build_log" 2>/dev/null || echo "0")
    if [ "$total_warnings" -gt 0 ]; then
        log_warn "Found $total_warnings warnings"

        echo "### Warnings: $total_warnings" >> "$error_summary_file"
        echo "" >> "$error_summary_file"
        echo "(Showing first 20)" >> "$error_summary_file"
        grep "^warning:" "$build_log" | head -20 | sed 's/^/- /' >> "$error_summary_file"
        echo "" >> "$error_summary_file"
    fi

    # Summary
    cat >> "$error_summary_file" << EOF

## Summary
- Total Errors: $total_errors
- Total Warnings: $total_warnings
- Log Size: $(wc -l < "$build_log") lines

## Most Common Error Patterns
EOF

    # Analyze most common error messages
    grep "^error:" "$build_log" 2>/dev/null | \
        sed 's/^error: //' | \
        cut -d ':' -f 1 | \
        sort | uniq -c | sort -rn | head -5 | \
        sed 's/^/- /' >> "$error_summary_file"

    echo "" >> "$error_summary_file"

    log_success "Error summary written to: $error_summary_file"
    log_metric "Total Errors" "$total_errors"
    log_metric "Total Warnings" "$total_warnings"

    return 0
}

# Detect SPM (Swift Package Manager) resolution errors
detect_spm_resolution_errors() {
    local build_log="$1"
    local error_summary_file="${2:-}"

    log_section "SPM Resolution Error Detection"

    if [ ! -f "$build_log" ]; then
        log_error "Build log not found: $build_log"
        return 1
    fi

    local spm_errors_found=0

    # Pattern 1: Package graph resolution errors
    if grep -q "error:.*package graph is unresolvable" "$build_log" 2>/dev/null; then
        log_error "SPM: Package graph resolution failure"
        spm_errors_found=$((spm_errors_found + 1))

        if [ -n "$error_summary_file" ]; then
            cat >> "$error_summary_file" << 'EOF'

### SPM Package Graph Resolution Failure

**Root Cause:** Swift Package Manager cannot resolve dependency versions.

**Common Causes:**
- Version conflicts between dependencies
- Dependency requires newer Swift version than project
- Circular dependencies in package graph

**Fix:**
1. Check Package.resolved for version conflicts
2. Update dependencies: `xcodebuild -resolvePackageDependencies`
3. Clean SPM cache: `rm -rf ~/Library/Caches/org.swift.swiftpm`

EOF
        fi
    fi

    # Pattern 2: Version conflicts
    if grep -q "error:.*version .* is required, but .* was resolved" "$build_log" 2>/dev/null; then
        log_error "SPM: Version conflict detected"
        spm_errors_found=$((spm_errors_found + 1))

        if [ -n "$error_summary_file" ]; then
            echo "### SPM Version Conflict" >> "$error_summary_file"
            echo "" >> "$error_summary_file"
            grep "error:.*version .* is required" "$build_log" | sed 's/^/- /' >> "$error_summary_file"
            echo "" >> "$error_summary_file"
        fi
    fi

    # Pattern 3: Git clone failures
    if grep -q "error:.*remote repository.*not found\|error:.*failed to clone" "$build_log" 2>/dev/null; then
        log_error "SPM: Git clone failure"
        spm_errors_found=$((spm_errors_found + 1))

        if [ -n "$error_summary_file" ]; then
            cat >> "$error_summary_file" << 'EOF'

### SPM Git Clone Failure

**Root Cause:** Cannot clone dependency repository.

**Common Causes:**
- Repository URL incorrect or inaccessible
- Network connectivity issues
- Git authentication failure
- Repository moved or deleted

**Fix:**
1. Verify repository URLs in Package.swift
2. Check Xcode Cloud network connectivity
3. For private repos, verify SSH keys in App Store Connect
4. Check repository still exists at specified URL

EOF
        fi
    fi

    # Pattern 4: Checksum mismatches
    if grep -q "error:.*checksum of downloaded.*does not match checksum" "$build_log" 2>/dev/null; then
        log_error "SPM: Checksum mismatch"
        spm_errors_found=$((spm_errors_found + 1))

        if [ -n "$error_summary_file" ]; then
            cat >> "$error_summary_file" << 'EOF'

### SPM Checksum Mismatch

**Root Cause:** Downloaded dependency doesn't match expected checksum.

**Common Causes:**
- Dependency updated but Package.resolved not updated
- Network corruption during download
- Cached corrupted version

**Fix:**
1. Update Package.resolved: `xcodebuild -resolvePackageDependencies`
2. Clean SPM cache: `rm -rf ~/Library/Caches/org.swift.swiftpm`
3. Delete DerivedData
4. Rebuild from scratch

EOF
        fi
    fi

    # Pattern 5: Missing critical dependencies
    if grep -q "error:.*no such module.*Supabase\|error:.*no such module.*Kingfisher" "$build_log" 2>/dev/null; then
        log_error "SPM: Critical dependencies not resolved (Supabase, Kingfisher)"
        spm_errors_found=$((spm_errors_found + 1))

        if [ -n "$error_summary_file" ]; then
            cat >> "$error_summary_file" << 'EOF'

### Critical Dependencies Not Resolved

**Root Cause:** Supabase or other packages not available.

**Affected Modules (Foodshare):**
- Supabase (Auth, Functions, PostgREST, Realtime, Storage)
- Kingfisher (Image caching)
- Skip (Cross-platform framework)

**Fix:**
1. Verify Package.swift dependencies section
2. Check Xcode Cloud can access repository
3. Clean and rebuild: Clean Build Folder in App Store Connect

EOF
        fi
    fi

    log_metric "SPM Errors Found" "$spm_errors_found"

    if [ $spm_errors_found -gt 0 ]; then
        return 1
    fi

    return 0
}

# Extract specific error with context
extract_error_context() {
    local build_log="$1"
    local error_pattern="$2"
    local context_lines="${3:-5}"

    log_debug "Extracting context for pattern: $error_pattern"

    if [ ! -f "$build_log" ]; then
        log_error "Build log not found: $build_log"
        return 1
    fi

    grep -B "$context_lines" -A "$context_lines" "$error_pattern" "$build_log" 2>/dev/null || {
        log_warn "Pattern not found in build log"
        return 1
    }

    return 0
}

# Categorize error type
categorize_error() {
    local error_message="$1"

    if echo "$error_message" | grep -q "cannot find type .* in scope"; then
        echo "TYPE_RESOLUTION"
    elif echo "$error_message" | grep -q "import.*not found\|module.*not found"; then
        echo "IMPORT_MISSING"
    elif echo "$error_message" | grep -q "@MainActor\|Sendable\|actor-isolated"; then
        echo "CONCURRENCY"
    elif echo "$error_message" | grep -q "Undefined symbols\|ld: symbol"; then
        echo "LINKER"
    elif echo "$error_message" | grep -q "code signing\|provisioning profile"; then
        echo "CODE_SIGNING"
    elif echo "$error_message" | grep -q "Package\|dependency"; then
        echo "DEPENDENCY"
    else
        echo "UNKNOWN"
    fi
}

# Get actionable fix for error category
get_error_fix() {
    local category="$1"

    case "$category" in
        TYPE_RESOLUTION)
            echo "This is likely a batch compilation cache issue. Clean Build Folder in App Store Connect."
            ;;
        IMPORT_MISSING)
            echo "Add missing import statements to affected files."
            echo "For @Observable types, add: import Observation"
            ;;
        CONCURRENCY)
            echo "Fix Swift 6 concurrency issues:"
            echo "- Ensure @MainActor annotations are correct"
            echo "- Check Sendable conformance for cross-actor types"
            ;;
        LINKER)
            echo "Linker errors indicate missing symbols or frameworks."
            echo "- Check that all dependencies are properly linked"
            echo "- Verify framework search paths"
            ;;
        CODE_SIGNING)
            echo "Code signing issues in Xcode Cloud:"
            echo "- Verify provisioning profile in App Store Connect"
            echo "- Check certificate validity"
            ;;
        *)
            echo "Review build log for specific error details"
            ;;
    esac
}

# Export functions
export -f parse_xcodebuild_errors
export -f detect_spm_resolution_errors
export -f extract_error_context
export -f categorize_error
export -f get_error_fix
