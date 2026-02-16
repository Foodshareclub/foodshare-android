---
inclusion: always
---

# Foodshare Project Structure

## Architecture Pattern

**Feature-Based Modular Architecture** with Clean Architecture layers:
- Features are independent and don't import other features
- All features can depend on Core module
- Each feature follows Domain/Data/Presentation layers

## Root Structure

```
Foodshare/
├── App/                      # Application entry point & global state
├── Core/                     # Shared infrastructure & design system
├── Features/                 # Feature modules (independent)
└── Resources/                # Assets, entitlements

FoodshareTests/               # Test targets
├── Unit/                     # Fast, isolated tests (70%)
├── Integration/              # Layer interaction tests (20%)
└── UITests/                  # End-to-end flows (10%)

supabase/                     # Backend (PostgreSQL, Edge Functions)
├── functions/                # Serverless Deno functions
├── migrations/               # Database schema migrations
└── types/                    # TypeScript type definitions

context/                      # Documentation & type system
docs/                         # Additional documentation
ci_scripts/                   # Xcode Cloud CI/CD scripts
```

## App Module

```
App/
├── FoodshareApp.swift        # @main entry point
├── AppState.swift            # Global app state (@Observable)
├── RootView.swift            # Root navigation
└── Configuration/
    └── Environment.swift     # Environment variables
```

## Core Module (Shared Infrastructure)

```
Core/
├── Design/                   # Liquid Glass Design System
│   ├── Tokens/               # Colors, Typography, Spacing
│   ├── Components/           # Reusable UI (Buttons, Cards, TextFields)
│   └── Modifiers/            # Custom ViewModifiers
├── Networking/               # HTTP client layer
├── Database/                 # Supabase client & services
├── Location/                 # Location services & geolocation
├── Storage/                  # Keychain, cache, UserDefaults
├── Extensions/               # Swift extensions
├── Utilities/                # Logger, validators, helpers
│   ├── Constants.swift       # App-wide constants
│   ├── DistanceUnit.swift    # Locale-aware distance (km/mi)
│   └── Logger.swift          # Logging utilities
└── Models/                   # Shared domain models
```

## Feature Module Structure

Each feature follows this template:

```
Features/[FeatureName]/
├── Domain/                   # Business logic (pure Swift)
│   ├── Models/               # Domain entities
│   ├── Repositories/         # Repository protocols
│   └── UseCases/             # Business operations
├── Data/                     # Data layer implementation
│   ├── Repositories/         # Concrete implementations
│   ├── DTOs/                 # Data Transfer Objects
│   └── Mappers/              # DTO ↔ Domain mapping
├── Presentation/             # UI layer
│   ├── Views/                # SwiftUI views
│   ├── ViewModels/           # @Observable ViewModels
│   ├── Components/           # Feature-specific UI
│   └── Navigation/           # Coordinators
└── Mocks/                    # Test fixtures
```

## Current Features

```
Features/
├── Authentication/           # Login, signup, password reset
├── Feed/                     # Browse food listings
├── Listing/                  # Create/edit listings
├── Map/                      # Map view with pins
├── Messaging/                # Real-time chat
├── Profile/                  # User profiles & settings
├── Search/                   # Search & filters
└── Shared/                   # Cross-feature views
    └── Views/
        ├── MainTabView.swift
        ├── CreateListingView.swift
        ├── MapView.swift
        ├── MessagingView.swift
        └── ProfileView.swift
```

## Naming Conventions

### Files
- **Swift files**: PascalCase matching primary type (`FoodListing.swift`)
- **Test files**: Append `Tests` (`FoodListingTests.swift`)
- **Mock files**: Prefix with `Mock` (`MockFoodListingRepository.swift`)
- **Extensions**: Use `+` notation (`Date+Extensions.swift`)
- **Fixtures**: Append `+Fixtures` (`FoodListing+Fixtures.swift`)

### Folders
- **Feature/module folders**: PascalCase (`FoodListings`, `Authentication`)
- **Technical folders**: lowercase (`models`, `views`, `tests`)

## Dependency Rules

✅ **Allowed**:
- Features → Core
- App → Everything

❌ **Forbidden**:
- Features → Features (no cross-feature imports)
- Core → Features

### Cross-Feature Communication

When features need to interact, use:
1. **Via App** (recommended): Pass data through parent views
2. **Via Shared Models in Core**: Reference via shared types
3. **Via App State**: Global state for shared data

## Design System Usage

```swift
// Colors
Color.DesignSystem.primary
Color.DesignSystem.background
Color.DesignSystem.glassBackground

// Typography
Font.DesignSystem.displayLarge
Font.DesignSystem.headlineLarge
Font.DesignSystem.bodyLarge

// Spacing
Spacing.sm  // 8pt
Spacing.md  // 16pt
Spacing.lg  // 24pt

// Components
GlassButton("Share Food", icon: "plus.circle.fill", style: .primary) { }
GlassTextField("Email", text: $email, icon: "envelope.fill")
FoodItemCard(foodItem: item) { /* handler */ }
```

## Best Practices

### Do's
- ✅ Keep features independent
- ✅ Put shared code in Core
- ✅ Follow Clean Architecture layers
- ✅ Write tests alongside code
- ✅ Use fixtures for test data
- ✅ Keep folders organized (max 4 levels deep)

### Don'ts
- ❌ Import features from other features
- ❌ Mix presentation and business logic
- ❌ Put feature-specific code in Core
- ❌ Skip tests for new code
- ❌ Hardcode values (use Constants)
- ❌ Create deep folder hierarchies
