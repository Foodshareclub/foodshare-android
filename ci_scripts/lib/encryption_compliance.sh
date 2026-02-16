#!/bin/bash
# chmod +x ci_scripts/lib/encryption_compliance.sh

# =============================================================================
# Encryption Compliance Documentation Generator
# =============================================================================
# Generates App Store encryption compliance documentation
# Called by CI scripts to ensure compliance docs are always up-to-date
# =============================================================================

generate_encryption_compliance_doc() {
    local output_file="${1:-docs/APP_STORE_ENCRYPTION_COMPLIANCE.md}"
    local output_dir
    output_dir=$(dirname "${output_file}")

    # Create directory if it doesn't exist
    if [ ! -d "${output_dir}" ]; then
        echo "Creating directory: ${output_dir}"
        mkdir -p "${output_dir}" || {
            echo "ERROR: Failed to create directory: ${output_dir}"
            return 1
        }
    fi

    echo "Generating encryption compliance documentation..."

    # Get build info
    local build_number="${CI_BUILD_NUMBER:-local}"
    local commit_hash
    commit_hash=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    cat > "${output_file}" << EOFMARKER
# App Store Encryption Compliance

## Export Compliance Question

**Question:** "What type of encryption algorithms does your app implement?"

**Answer:** None of the algorithms mentioned above

## Rationale

Foodshare uses only standard encryption provided by:

### 1. Apple's Operating System (iOS)
- **HTTPS/TLS**: All network communication uses standard TLS encryption
- **Keychain**: Secure credential storage using iOS Keychain
- **URLSession**: Standard HTTPS connections

### 2. Standard Protocols (Not Custom)
- **OAuth 2.0**: Standard authentication protocol via Supabase Auth
  - Uses standard JWT tokens
  - HTTPS transport encryption

### 3. Standard Hashing (Not Encryption)
- **CryptoKit SHA256**: Used only for Apple Sign In nonce hashing
  - This is hashing, not encryption
  - Standard algorithm (FIPS 180-4)
  - Provided by Apple's CryptoKit framework

## Dependencies Encryption Analysis

### Supabase SDK
- Uses standard HTTPS for all API calls
- No custom encryption algorithms
- Relies on iOS URLSession and TLS

### Kingfisher (Image Caching)
- Standard HTTPS image downloads
- No encryption beyond iOS standard

### Lottie (Animations)
- No encryption - JSON animation files only

## Conclusion

**No export compliance documentation required**

The app does not implement:
- Proprietary encryption algorithms
- Non-standard encryption algorithms
- Custom encryption beyond Apple's OS encryption

All encryption is either:
1. Provided by Apple's operating system (iOS)
2. Standard protocols accepted by international bodies (IETF, IEEE)

## App Store Connect Answer

When submitting to App Store Connect, select:

**"None of the algorithms mentioned above"**

This means:
- No proprietary encryption
- No standard encryption beyond what iOS provides
- No export compliance documentation needed

## References

- [Apple's Encryption and Export Compliance](https://developer.apple.com/documentation/security/complying_with_encryption_export_regulations)

---

**Generated:** ${timestamp}
**Build:** ${build_number}
**Commit:** ${commit_hash}
EOFMARKER

    if [ -f "${output_file}" ]; then
        echo "OK: Encryption compliance documentation generated: ${output_file}"
        return 0
    else
        echo "ERROR: Failed to generate encryption compliance documentation"
        return 1
    fi
}
