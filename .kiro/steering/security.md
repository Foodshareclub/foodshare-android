---
inclusion: always
---

# Security Guidelines

## Authentication & Authorization

### Supabase Auth Integration

```swift
// Use PKCE flow for secure authentication
let supabase = SupabaseClient(
    supabaseURL: URL(string: Environment.supabaseURL)!,
    supabaseKey: Environment.supabaseAnonKey,
    options: SupabaseClientOptions(
        auth: .init(
            storage: KeychainStorage(),
            flowType: .pkce,
            autoRefreshToken: true
        )
    )
)
```

### Token Storage

- ✅ Store auth tokens in Keychain (never UserDefaults)
- ✅ Use KeychainStorage for Supabase session persistence
- ✅ Enable auto-refresh for tokens
- ❌ Never log auth tokens or sensitive data
- ❌ Never commit API keys to version control

```swift
// Secure token storage
final class KeychainStorage: AuthStorage {
    func store(key: String, value: Data) throws {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: value,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]
        
        SecItemDelete(query as CFDictionary)
        let status = SecItemAdd(query as CFDictionary, nil)
        
        guard status == errSecSuccess else {
            throw KeychainError.storeFailed
        }
    }
}
```

### Biometric Authentication Security

The app implements enterprise-grade biometric security with multiple protection layers:

```swift
// BiometricAuthService provides:
// - Jailbreak detection (blocks biometrics on compromised devices)
// - Biometric change detection (detects new fingerprints/Face ID)
// - Configurable security levels (Standard/Enhanced/Maximum)
// - Failed attempt tracking with lockout

// Check for device compromise before biometric operations
if biometricService.isDeviceCompromised {
    // Don't allow biometric authentication
    return
}

// Check for biometric data changes
if biometricService.hasBiometricChanged() {
    showBiometricChangedAlert = true
    biometricService.disableBiometrics()
    return
}

// Configure security level for lock timeout
biometricService.securityLevel = .enhanced // 10 second timeout
```

**Security Levels:**
- `standard`: Lock after 30 seconds in background
- `enhanced`: Lock after 10 seconds in background  
- `maximum`: Lock immediately when leaving app

**Jailbreak Detection Checks:**
- Common jailbreak file paths (Cydia, MobileSubstrate, etc.)
- Sandbox escape attempts (write outside app directory)
- Suspicious URL schemes (cydia://)

**Biometric Change Detection:**
- Saves `evaluatedPolicyDomainState` on enrollment
- Compares current state on each authentication
- Disables biometrics if fingerprints/Face ID changed

## Data Validation

### Input Validation

Always validate user input before processing:

```swift
struct FoodListingValidator {
    func validate(_ listing: FoodListing) throws {
        // Title validation
        guard listing.title.count >= 3 && listing.title.count <= 100 else {
            throw ValidationError.invalidTitle
        }
        
        // Sanitize HTML/scripts
        guard !listing.title.contains("<script>") else {
            throw ValidationError.invalidInput
        }
        
        // Expiry date validation
        guard let expiryDate = listing.expiryDate,
              expiryDate > Date() else {
            throw ValidationError.expiredDate
        }
        
        // Image count validation
        guard listing.imageUrls.count >= 1 && listing.imageUrls.count <= 5 else {
            throw ValidationError.invalidImageCount
        }
        
        // Location validation
        guard listing.pickupLatitude >= -90 && listing.pickupLatitude <= 90,
              listing.pickupLongitude >= -180 && listing.pickupLongitude <= 180 else {
            throw ValidationError.invalidLocation
        }
    }
}
```

### Email Validation

```swift
extension String {
    var isValidEmail: Bool {
        let emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$"
        let predicate = NSPredicate(format: "SELF MATCHES %@", emailRegex)
        return predicate.evaluate(with: self)
    }
}
```

## Row Level Security (RLS)

All database operations respect RLS policies:

### User Profile Access
```sql
-- Users can only update their own profile
CREATE POLICY "Users can update own profile"
ON profiles_foodshare FOR UPDATE
TO authenticated
USING (id = auth.uid());
```

### Food Items Access
```sql
-- Anyone can view available items
-- Only owner can update/delete
CREATE POLICY "Public read available items"
ON food_items FOR SELECT
TO authenticated
USING (status = 'available' OR user_id = auth.uid());

CREATE POLICY "Owner can update items"
ON food_items FOR UPDATE
TO authenticated
USING (user_id = auth.uid());
```

### Conversation Privacy
```sql
-- Only participants can view messages
CREATE POLICY "Participants can view messages"
ON messages FOR SELECT
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM conversations
        WHERE conversations.id = messages.conversation_id
        AND (conversations.owner_id = auth.uid() 
             OR conversations.requester_id = auth.uid())
    )
);
```

## Secure Communication

### HTTPS Only

App Transport Security (ATS) enforced in Info.plist:

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <false/>
</dict>
```

### API Key Management

```swift
// ✅ Use environment variables
enum Environment {
    static var supabaseURL: String {
        guard let url = ProcessInfo.processInfo.environment["SUPABASE_URL"] else {
            fatalError("SUPABASE_URL not set")
        }
        return url
    }
    
    static var supabaseAnonKey: String {
        guard let key = ProcessInfo.processInfo.environment["SUPABASE_ANON_KEY"] else {
            fatalError("SUPABASE_ANON_KEY not set")
        }
        return key
    }
}

// ❌ Never hardcode keys
// let apiKey = "sbp_abc123..." // DON'T DO THIS
```

## Image Upload Security

### File Validation

```swift
struct ImageValidator {
    static let maxFileSize: Int = 5_000_000 // 5MB
    static let allowedTypes: Set<String> = ["image/jpeg", "image/png", "image/heic"]
    
    func validate(_ imageData: Data, mimeType: String) throws {
        // Check file size
        guard imageData.count <= Self.maxFileSize else {
            throw ValidationError.fileTooLarge
        }
        
        // Check file type
        guard Self.allowedTypes.contains(mimeType) else {
            throw ValidationError.invalidFileType
        }
        
        // Verify it's actually an image
        guard UIImage(data: imageData) != nil else {
            throw ValidationError.corruptedImage
        }
    }
}
```

### Secure Upload

```swift
func uploadImage(_ imageData: Data) async throws -> String {
    // Validate before upload
    try ImageValidator().validate(imageData, mimeType: "image/jpeg")
    
    // Generate unique filename
    let filename = "\(UUID().uuidString).jpg"
    
    // Upload to Supabase Storage with RLS
    let path = try await supabase.storage
        .from("food-images")
        .upload(
            path: filename,
            file: imageData,
            options: FileOptions(
                cacheControl: "3600",
                contentType: "image/jpeg"
            )
        )
    
    return path
}
```

## Privacy Protection

### PrivacyProtectionService

The app includes enterprise-grade privacy protection via `PrivacyProtectionService`:

```swift
// Access the singleton
let privacyService = PrivacyProtectionService.shared

// Features:
// - Privacy blur when app goes to background
// - Screen recording detection with warning banner
// - Clipboard auto-clear for sensitive data
// - Session timeout management
```

**Privacy Blur:**
- Automatically shows blur overlay when app enters background
- Displays app icon with lock indicator
- Enabled by default, user-configurable

**Screen Recording Detection:**
- Detects when screen recording is active (`UIScreen.main.isCaptured`)
- Posts `.screenRecordingDetected` notification
- Shows warning banner via `ScreenRecordingAlertModifier`

```swift
// Add screen recording warning to any view
struct MyView: View {
    var body: some View {
        ContentView()
            .screenRecordingWarning()  // Shows banner when recording detected
    }
}
```

**Clipboard Security:**
```swift
// Secure copy with auto-clear (default 60 seconds)
privacyService.secureCopy("sensitive-data")

// Manual clear
privacyService.clearClipboard()
```

**Session Timeout:**
- Configurable timeout duration (15 min to never)
- Posts `.sessionExpired` notification when expired
- Call `updateActivity()` on user interaction

```swift
// Session timeout options
enum SessionTimeoutOption {
    case fifteenMinutes  // 15 min
    case oneHour         // 1 hour
    case fourHours       // 4 hours
    case twentyFourHours // 24 hours (default)
    case oneWeek         // 7 days
    case never           // No timeout
}
```

### Location Privacy

```swift
// Only share approximate location publicly
struct LocationPrivacy {
    // Round coordinates to ~100m precision
    static func approximateLocation(_ location: CLLocation) -> CLLocation {
        let precision = 0.001 // ~100m
        let lat = round(location.coordinate.latitude / precision) * precision
        let lon = round(location.coordinate.longitude / precision) * precision
        return CLLocation(latitude: lat, longitude: lon)
    }
}

// Share exact address only after claim
func shareExactAddress(for listing: FoodListing, with user: User) async throws {
    guard listing.claimedBy == user.id else {
        throw SecurityError.unauthorized
    }
    // Share full address in private message
}
```

### Personal Data Handling

```swift
// Minimize data collection
struct UserProfile {
    let id: UUID
    let nickname: String // Not real name
    let avatarUrl: String?
    // ❌ Don't store: SSN, credit cards, passwords in plain text
}

// Respect user privacy preferences
struct PrivacySettings {
    var showLocation: Bool = true
    var showEmail: Bool = false
    var allowNotifications: Bool = true
}
```

## Rate Limiting

Implement client-side rate limiting:

```swift
actor RateLimiter {
    private var lastRequestTime: Date?
    private let minimumInterval: TimeInterval = 1.0 // 1 second
    
    func checkRateLimit() async throws {
        if let lastTime = lastRequestTime {
            let elapsed = Date().timeIntervalSince(lastTime)
            if elapsed < minimumInterval {
                throw AppError.rateLimitExceeded
            }
        }
        lastRequestTime = Date()
    }
}

// Usage
let rateLimiter = RateLimiter()

func createListing(_ listing: FoodListing) async throws {
    try await rateLimiter.checkRateLimit()
    // Proceed with creation
}
```

## Error Handling

### Secure Error Messages

```swift
// ✅ User-friendly, non-revealing errors
enum AppError: LocalizedError {
    case unauthorized
    case networkError
    case validationError(String)
    
    var errorDescription: String? {
        switch self {
        case .unauthorized:
            return "Please log in to continue"
        case .networkError:
            return "Connection failed. Please try again."
        case .validationError(let message):
            return message
        }
    }
}

// ❌ Don't expose internal details
// "Database connection failed: postgres://user:pass@host:5432/db"
// "SQL error: SELECT * FROM users WHERE password = '...'"
```

### Logging Security

```swift
// ✅ Safe logging
logger.info("User \(userId) created listing")

// ❌ Never log sensitive data
// logger.debug("Auth token: \(token)")
// logger.debug("Password: \(password)")
// logger.debug("Credit card: \(cardNumber)")
```

## Dependency Security

### Package Verification

- Only use official Supabase Swift SDK
- Verify package checksums
- Keep dependencies updated
- Review dependency changes in PRs

```swift
// Package.swift
.package(url: "https://github.com/supabase/supabase-swift.git", from: "2.0.0")
```

### Vulnerability Scanning

Run security checks regularly:
```bash
# Check for known vulnerabilities
swift package audit

# Update dependencies
swift package update
```

## Security Checklist

### Before Release
- [ ] All API keys in environment variables
- [ ] Keychain storage for auth tokens
- [ ] Input validation on all user inputs
- [ ] RLS policies tested and verified
- [ ] HTTPS enforced (ATS enabled)
- [ ] Image upload size limits enforced
- [ ] Location privacy implemented
- [ ] Error messages don't leak sensitive info
- [ ] Dependencies updated and scanned
- [ ] Security review completed
- [ ] Biometric security features tested
- [ ] Jailbreak detection verified
- [ ] Biometric change detection working

### Regular Audits
- [ ] Review RLS policies monthly
- [ ] Update dependencies quarterly
- [ ] Rotate API keys annually
- [ ] Review access logs for anomalies
- [ ] Test authentication flows
- [ ] Verify data encryption at rest
- [ ] Test biometric security on real devices

## Incident Response

If a security issue is discovered:

1. **Assess Impact**: Determine scope and severity
2. **Contain**: Disable affected features if needed
3. **Fix**: Patch vulnerability immediately
4. **Notify**: Inform affected users if data exposed
5. **Review**: Conduct post-mortem and update policies

## Resources

- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [Apple Security Guidelines](https://developer.apple.com/security/)
- [Supabase Security Best Practices](https://supabase.com/docs/guides/auth/row-level-security)
