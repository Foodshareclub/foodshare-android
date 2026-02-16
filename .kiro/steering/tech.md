---
inclusion: always
---

# Foodshare Technical Stack

## Build System & Tools

- **Xcode Project**: Foodshare.xcodeproj (not SPM workspace)
- **Swift Version**: 6.2 with strict concurrency checking
- **Minimum iOS**: 17.0+
- **Package Manager**: Swift Package Manager for dependencies
- **CI/CD**: Xcode Cloud with automated versioning

## Tech Stack

### Frontend (iOS)
- **UI Framework**: SwiftUI (declarative UI)
- **State Management**: `@Observable` macro (iOS 17+)
- **Architecture**: MVVM + Clean Architecture
- **Concurrency**: async/await, actors for thread safety
- **Design System**: Liquid Glass v26 (glassmorphism)

### Backend (Supabase)
- **Database**: PostgreSQL 15 with PostGIS for geospatial queries
- **Authentication**: Supabase Auth with PKCE flow
- **Storage**: Supabase Storage for images
- **Real-time**: Supabase Realtime for chat and live updates
- **Edge Functions**: Deno/TypeScript serverless functions
- **Vault**: Supabase Vault for secure secret management
- **Caching**: Upstash Redis REST API (credentials from Vault)
- **Email**: Resend REST API (credentials from Vault)
- **Maps**: Native MapKit (Apple Maps)

### Swift Package Dependencies

| Package | Version | Purpose | Status |
|---------|---------|---------|--------|
| supabase-swift | 2.37.0 (exact) | Backend (auth, database, storage, realtime, vault) | ✅ Active |
| lottie-spm | 4.5.0+ | High-quality animations | ✅ Active |
| Kingfisher | 7.10.0+ | Image loading and caching | ✅ Active |
| livekit-client-sdk-swift | 2.9.0+ | Real-time video/audio (future) | ⏸️ Temporarily Removed |

```swift
// Package.swift dependencies
.package(url: "https://github.com/supabase/supabase-swift.git", exact: "2.37.0")
.package(url: "https://github.com/airbnb/lottie-spm.git", from: "4.5.0")
.package(url: "https://github.com/onevcat/Kingfisher.git", from: "7.10.0")
// LiveKit temporarily removed to fix package resolution hanging
// .package(url: "https://github.com/livekit/client-sdk-swift.git", from: "2.9.0")
```

**Note**: Upstash Redis and Resend don't have official Swift packages. We use their REST APIs directly with credentials fetched from Supabase Vault. See `docs/VAULT_INTEGRATION.md` for details.

**Kingfisher Benefits**:
- Automatic memory and disk caching for better performance
- Progressive image loading with placeholders
- Image processing and transformations
- Prefetching support for smooth scrolling
- Better than AsyncImage for lists with many images

## Common Commands

### Build & Run
```bash
# Open project
open Foodshare.xcodeproj

# Build from command line (with workaround for dependency files)
./scripts/build_workaround.sh

# Build directly with xcodebuild
xcodebuild -scheme Foodshare -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max'

# Run tests
xcodebuild test -scheme Foodshare -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max'
```

### Build Workaround Script
The `scripts/build_workaround.sh` script handles Swift compiler dependency file issues:
- Creates necessary build directories
- Generates `.d` dependency files
- Builds and installs to simulator
- Launches the app automatically

Use this when encountering Swift compiler errors about missing dependency files.

### Version Management
```bash
# Bump version (patch: 3.0.0 → 3.0.1)
./ci_scripts/utils/bump_version_smart.sh patch

# Bump minor version (3.0.0 → 3.1.0)
./ci_scripts/utils/bump_version_smart.sh minor

# Bump major version (3.0.0 → 4.0.0)
./ci_scripts/utils/bump_version_smart.sh major
```

### Supabase (Backend)
```bash
# Install Supabase CLI
brew install supabase/tap/supabase

# Start local Supabase
npx supabase start

# Apply migrations
npx supabase db push

# Deploy edge functions
npx supabase functions deploy

# View local dashboard
open http://localhost:54323
```

### Code Quality
```bash
# SwiftLint (configured in .swiftlint.yml)
swiftlint lint --strict

# SwiftFormat (configured in .swiftformat)
swiftformat --swiftversion 5.9 .

# Git hooks (configured in .lefthook.yml)
lefthook install
```

## Environment Configuration

### Supabase Configuration (Required)

Only Supabase credentials are needed in environment variables:

```swift
enum AppEnvironment {
    // Supabase (required)
    static var supabaseURL: String? {
        ProcessInfo.processInfo.environment["SUPABASE_URL"]
    }
    
    static var supabasePublishableKey: String? {
        ProcessInfo.processInfo.environment["SUPABASE_PUBLISHABLE_KEY"]
    }
}
```

### Third-Party Service Credentials (Vault)

All other service credentials are stored in **Supabase Vault** and fetched at runtime:
- `UPSTASH_REDIS_URL` - Upstash Redis REST endpoint
- `UPSTASH_REDIS_TOKEN` - Upstash Redis REST token
- `RESEND_API_KEY` - Resend email API key

**Setup**: Add secrets in Supabase Dashboard → Settings → Vault

**Usage**:
```swift
let vaultService = VaultService(supabase: supabase)
let redis = try await vaultService.getRedisClient()
let email = try await vaultService.getEmailClient()
```

See `docs/VAULT_INTEGRATION.md` for complete setup and usage instructions.

## Testing Strategy

- **70% Unit Tests**: ViewModels, Use Cases, Repositories (fast, isolated)
- **20% Integration Tests**: Layer interactions, Supabase integration
- **10% UI Tests**: Critical user flows only (slow, run before release)
- **Target Coverage**: 80%+ for business logic, 70%+ overall

## Security

- Keychain storage for auth tokens
- HTTPS-only (ATS enforced)
- Row Level Security (RLS) in Supabase
- Input validation throughout
- Secure file upload with size limits
