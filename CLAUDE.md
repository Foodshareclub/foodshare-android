# FoodShare App (Cross-Platform)

**Version:** 3.0.2 | **Framework:** Skip Fuse | **Platforms:** iOS 17+ & Android 28+ | **Swift:** 6.3 | **Status:** Production

> Unified cross-platform FoodShare app using Skip Fuse. Single Swift codebase builds native iOS and Android apps.

---

## Quick Reference

```bash
# iOS Build (Xcode)
open Darwin/FoodShare.xcodeproj    # Open in Xcode
# Build via Xcode scheme: FoodShare

# Android Build
cd Android && ./gradlew assembleDebug    # Build debug APK
cd Android && ./gradlew installDebug     # Install to device/emulator

# Swift Package
swift build                              # Build Swift package
swift test                               # Run tests
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Swift 6.3 |
| UI Framework | SwiftUI (via Skip Fuse → Jetpack Compose on Android) |
| Cross-Platform | Skip Fuse 1.7.2 |
| Backend | Self-hosted Supabase (supabase-swift 2.41+) |
| Images | Kingfisher (iOS) / Coil (Android, via Skip) |
| Design System | Liquid Glass (137 components) |
| Architecture | Clean Architecture + MVVM + @Observable |

---

## Project Structure

```
foodshare-app/
├── Package.swift                    # SPM + Skip plugin
├── Skip.env                         # Skip project metadata
├── Sources/FoodShare/               # ALL Swift source code
│   ├── FoodShareApp.swift           # App entry point
│   ├── App/                         # App-level coordination
│   ├── Core/                        # Core infrastructure
│   │   ├── Analytics/               # Event tracking
│   │   ├── Architecture/            # Base protocols
│   │   ├── Cache/                   # Caching layer
│   │   ├── Design/                  # Liquid Glass (137 files)
│   │   ├── Errors/                  # Error handling
│   │   ├── Extensions/              # Swift extensions
│   │   ├── FeatureFlags/            # Feature flags
│   │   ├── Localization/            # i18n (21 languages)
│   │   ├── Networking/              # APIClient, Edge Functions
│   │   ├── Performance/             # Performance monitoring
│   │   ├── Router/                  # Navigation
│   │   ├── Security/                # Keychain, biometrics
│   │   └── Services/                # Auth, location, etc.
│   ├── Features/                    # Feature modules (24)
│   │   ├── Authentication/
│   │   ├── Feed/
│   │   ├── Listing/
│   │   ├── Messaging/
│   │   ├── Profile/
│   │   ├── Map/
│   │   ├── Reviews/
│   │   ├── Challenges/
│   │   ├── Forum/
│   │   └── ...
│   ├── Skip/                        # Skip configuration
│   │   └── skip.yml                 # Transpilation settings
│   └── Resources/                   # Assets, strings
├── Darwin/                          # iOS-specific
│   ├── FoodShare.xcodeproj          # Xcode project
│   ├── FoodShare.xcconfig           # Build settings
│   └── Info.plist
├── Android/                         # Android-specific
│   ├── app/build.gradle.kts         # Android build
│   ├── settings.gradle.kts
│   └── gradle/
└── Tests/FoodShareTests/            # Unit tests
```

---

## Architecture

### Clean Architecture + MVVM

```
Presentation (SwiftUI Views + ViewModels)
    ↓
Domain (Protocols + Models)
    ↓
Data (API Services + Repositories)
    ↓
Backend (Supabase Edge Functions)
```

### Key Patterns

- **API-First**: All data through Edge Functions via `APIClient.shared`
- **Supabase Fallback**: Direct Supabase in `catch` blocks for resilience
- **@Observable ViewModels**: `@MainActor` with `@Observable` macro
- **Design Tokens**: Liquid Glass colors, spacing, typography throughout

### Platform-Specific Code

Use `#if !SKIP` guards for iOS-only features:

```swift
#if !SKIP
import Lottie
#endif

struct AnimatedView: View {
    var body: some View {
        #if !SKIP
        LottieView(animation: .named("loading"))
        #else
        ProgressView()
        #endif
    }
}
```

**Guarded Features:**
- Lottie animations
- Metal shaders
- UIKit haptics (UIImpactFeedbackGenerator)
- Keychain (→ EncryptedSharedPreferences on Android)
- StoreKit IAP
- DeviceCheck

---

## Networking

### API Client

All API calls go through `APIClient.shared` to Edge Functions:

```swift
// Pattern: API-first with Supabase fallback
let items = try await APIClient.shared.request(
    endpoint: "api-v1-products",
    method: .GET,
    queryParams: ["category": "fruits"]
)
```

### Response Envelope

```swift
struct EdgeResponse<T: Decodable>: Decodable {
    let success: Bool
    let data: T?
    let error: String?
    let pagination: Pagination?
}
```

### Supabase Client

```swift
// Always use:
AuthenticationService.shared.supabase
// NEVER: SupabaseManager.shared
```

---

## Design System (Liquid Glass)

137 design files in `Core/Design/`:
- **Tokens**: LiquidGlassColors, LiquidGlassSpacing, LiquidGlassTypography
- **Components**: GlassButton, GlassCard, GlassTextField, GlassBottomSheet, etc.
- **Themes**: Brand, Coral, Forest, Midnight, Monochrome, Nature, Ocean, Sunset
- **Animations**: ProMotion 120Hz, spring animations, hero transitions
- **Effects**: Metal shaders (iOS-only, guarded with #if !SKIP)

---

## Backend

Shared backend at `/Users/organic/dev/work/foodshare/foodshare-backend/`:
- 27 Edge Functions (Deno)
- Self-hosted Supabase (152.53.136.84)
- API: https://api.foodshare.club
- `supabase/` symlink → `../foodshare-backend/supabase/`

---

## Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| skip | 1.7.2 | Cross-platform framework |
| skip-fuse-ui | 1.0.0 | SwiftUI → Compose bridge |
| supabase-swift | 2.41.0 | Backend client |
| Kingfisher | 8.6.2 | Image loading (iOS) |

---

## Environment

Self-hosted Supabase:
- Studio: https://studio.foodshare.club
- API: https://api.foodshare.club

---

## Important Rules

1. **Never use JSR imports** in Edge Functions (hangs on cold start)
2. **Never `console.log`** in Edge Functions (use structured `logger`)
3. **Always use `AuthenticationService.shared.supabase`** for Supabase client
4. **Always use `#if !SKIP`** for iOS-only code (Metal, UIKit, Lottie, etc.)
5. **Use `.interpolatingSpring`** for animations (ProMotion 120Hz)
6. **Keep `.easeInOut` only inside `.repeatForever()`**
7. **Supabase `.select()` strings**: No spaces between fields (`"id,name"` not `"id, name"`)
8. **Don't run xcodebuild via CLI** — no simulator available, builds will hang

---

## Migration Notes

This project was created from `foodshare-android/` (Feb 12, 2026) as a Skip Fuse cross-platform app.
The original native iOS project is at `foodshare-ios/` (renamed to `foodshare-ios-legacy/` after migration).

**Last Updated:** February 2026
