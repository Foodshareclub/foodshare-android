# Built with Opus 4.6: FoodShare Cross-Platform App

> Hackathon Submission | Feb 10-16, 2026 | Cerebral Valley x Anthropic

---

## Problem Statement

**Break the Barriers** -- Expert knowledge, essential tools, AI's benefits -- take something powerful that's locked behind expertise, cost, language, or infrastructure and put it in everyone's hands.

---

## TL;DR

**FoodShare** is a production-grade cross-platform app built from scratch with Claude Code (Opus 4.6). A single Swift codebase compiles to both iOS and Android via **Skip Fuse** -- 584 Swift files, 24 feature modules, 29 core infrastructure modules, a 137-component design system, and 35 backend Edge Functions. One solo developer, one week, ~198K lines of code.

---

## The Problem

Food waste is a massive global problem. FoodShare connects people with surplus food to those who need it. Reaching both iOS and Android users (72% of the global market) traditionally requires separate engineering teams or compromising on framework-specific limitations.

**The challenge:** Build a full-featured, production-grade app that runs natively on both platforms from a single codebase -- without sacrificing native UI quality, offline capability, or real-time features.

**Why "Break the Barriers":** Cross-platform mobile development at this scale is locked behind large teams, months of engineering time, and deep platform expertise. Claude Code breaks that barrier -- a solo developer shipped a complete cross-platform app with 24 features, 29 infrastructure modules, and a custom design system in one week.

---

## What We Built

### FoodShare Cross-Platform App

A full-featured food-sharing app running on iOS and Android from a single Swift codebase:

- **24 feature modules** -- Feed, Search, Map, Messaging, Profile, Listings, Reviews, Challenges, Forum, Donations, Community Fridges, Notifications, Analytics, and more
- **29 core infrastructure modules** -- Networking, caching, sync, analytics, accessibility, localization (21 languages), security, performance monitoring, feature flags, and more
- **137-component Liquid Glass design system** -- Custom glassmorphism theme with 8 color themes, design tokens, and ProMotion 120Hz animations
- **35 Supabase Edge Functions** -- Auth, search, AI recommendations, geocoding, notifications, email failover, Telegram/WhatsApp bots
- **Skip Fuse cross-platform** -- Single Swift codebase → native SwiftUI on iOS, native Jetpack Compose on Android

### Architecture

```
┌──────────────────────────────────────────────────┐
│           Supabase Backend (35 Edge Functions)    │
│  PostgreSQL + PostGIS | Edge Functions | Auth     │
│  RLS Policies | Storage | Realtime               │
└──────────────────────┬───────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │                           │
┌────────▼────────┐        ┌─────────▼────────┐
│    iOS App      │        │   Android App    │
│   (SwiftUI)     │        │ (Compose via     │
│                 │        │  Skip Fuse)      │
├─────────────────┤        ├──────────────────┤
│                                              │
│     Single Swift Codebase (584 files)        │
│     Sources/FoodShare/                       │
│     • 24 Features  • 29 Core modules         │
│     • 137 Design components                  │
│     • Clean Architecture + MVVM              │
│                                              │
└──────────────────────────────────────────────┘
```

---

## How Claude Code Made This Possible

### The Scale

| Metric | Count |
|--------|-------|
| Swift source files | 584 |
| Lines of Swift code | ~198,000 |
| Feature modules | 24 |
| Core infrastructure modules | 29 |
| Design system components | 137 |
| Backend Edge Functions | 35 |
| Supported languages (i18n) | 21 |
| Platforms shipped | 2 (iOS + Android) |

### Claude Code Workflow

Claude Code was the primary development tool for the entire project:

1. **Architecture Design** -- Claude Code analyzed the existing iOS codebase and designed the cross-platform architecture with Clean Architecture (MVVM + @Observable), establishing layer conventions that held across all 584 files

2. **Feature Scaffolding** -- For each of the 24 features, Claude Code generated the full stack: View → ViewModel → Repository → API Service, following the same patterns without drift

3. **Cross-Platform Compatibility** -- Claude Code learned Skip Fuse boundaries and proactively guarded iOS-only APIs (Metal shaders, UIKit haptics, Keychain, Lottie) with `#if !SKIP` while finding cross-platform alternatives

4. **Design System** -- Claude Code built the entire 137-component Liquid Glass design system: tokens, atoms, molecules, organisms across 8 color themes with ProMotion-optimized animations

5. **Backend** -- Claude Code built 35 Edge Functions with consistent patterns: `createAPIHandler`, structured logging, response envelopes, and multi-provider failover

6. **Infrastructure** -- Claude Code built all 29 core modules (networking with retry + fallback, caching, sync, analytics, accessibility, localization in 21 languages, feature flags, security, performance monitoring)

### What Makes This Different

This isn't a simple CRUD app. Claude Code tackled genuinely complex engineering:

- **API-first with fallback** -- Every data operation goes through Edge Functions via `APIClient.shared`, with automatic Supabase direct-query fallback in catch blocks for resilience

- **Liquid Glass design system** -- 137 components with glassmorphism effects, 8 themes, design tokens (spacing, corner radius, typography), and spring animations tuned for 120Hz ProMotion displays

- **Real-time features** -- Supabase Realtime subscriptions for messaging, notifications, and live feed updates

- **Offline sync** -- Delta sync with conflict resolution across platforms

- **21-language localization** -- Full i18n infrastructure with locale-aware formatting

- **Self-hosted infrastructure** -- Not a managed service -- self-hosted Supabase on VPS with Cloudflare tunnel, 35 custom Edge Functions

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Swift 6.1 |
| Cross-Platform | Skip Fuse 1.7.2 |
| UI (iOS) | SwiftUI |
| UI (Android) | Jetpack Compose (via Skip transpilation) |
| Architecture | Clean Architecture + MVVM + @Observable |
| Backend | Self-hosted Supabase (Deno Edge Functions) |
| Database | PostgreSQL + PostGIS |
| Auth | Supabase Auth |
| Images | Kingfisher (iOS) / Coil (Android via Skip) |
| Design System | Liquid Glass (137 components, 8 themes) |
| Dependencies | supabase-swift 2.41+, skip-fuse-ui 1.0.0 |

---

## Project Structure

```
foodshare-app/
├── Package.swift                    # SPM + Skip plugin
├── Sources/FoodShare/               # Single Swift codebase
│   ├── FoodShareApp.swift           # App entry point
│   ├── Core/                        # 29 infrastructure modules
│   │   ├── Networking/              # APIClient + Edge Function layer
│   │   ├── Design/                  # Liquid Glass (137 components)
│   │   ├── Cache/                   # Memory + disk caching
│   │   ├── Sync/                    # Delta sync engine
│   │   ├── Localization/            # 21 languages
│   │   ├── Analytics/               # Event tracking
│   │   ├── Security/                # Keychain, biometrics
│   │   ├── Performance/             # ProMotion monitoring
│   │   ├── Accessibility/           # WCAG compliance
│   │   └── ...                      # 20 more modules
│   ├── Features/                    # 24 feature modules
│   │   ├── Feed/                    # Main food feed
│   │   ├── Map/                     # Map discovery (PostGIS)
│   │   ├── Messaging/               # Real-time chat
│   │   ├── Listing/                 # Create/manage listings
│   │   ├── Forum/                   # Community forums
│   │   ├── Challenges/              # Gamified challenges
│   │   ├── Reviews/                 # Rating system
│   │   ├── Donation/                # Food donations
│   │   └── ...                      # 16 more features
│   └── Resources/                   # Assets, strings
├── Darwin/                          # iOS build (Xcode)
├── Android/                         # Android build (Gradle)
└── supabase/                        # Backend (symlink)
```

---

## Key Innovations

### 1. Skip Fuse at Production Scale

Skip Fuse is a new framework for writing cross-platform apps in Swift. FoodShare is one of the largest Skip Fuse apps in existence:

- **584 Swift files** compiling to both iOS and Android
- **24 feature modules** with full native UI on both platforms
- **137 design components** that render as SwiftUI on iOS and Jetpack Compose on Android
- **Platform-specific guards** (`#if !SKIP`) for iOS-only APIs with cross-platform alternatives

### 2. Full-Stack Solo Development

Claude Code enabled a single developer to build and ship every layer:

- **Frontend:** 24 features with native UI, offline support, real-time updates
- **Backend:** 35 Edge Functions with auth, search, AI, geocoding, notifications
- **Infrastructure:** Caching, sync, analytics, accessibility, localization, security
- **Design:** 137-component design system with 8 themes and 120Hz animations

### 3. Claude Code as Force Multiplier

A single developer built what would typically require a team of 4-6 engineers:

- ~198K lines of Swift code in one week
- Consistent architecture across 584 files with zero pattern drift
- Full design system, networking layer, and backend -- not just UI screens
- Cross-platform compatibility handled proactively, not as an afterthought

---

## Running the Project

### Prerequisites

- Xcode 16+ (iOS)
- Android Studio Ladybug+ (Android)
- Swift 6.1

### Quick Start

```bash
git clone https://github.com/Foodshareclub/foodshare-app.git
cd foodshare-app

# iOS
open Darwin/FoodShare.xcodeproj

# Android
cd Android && ./gradlew installDebug
```

---

## Links

| Resource | URL |
|----------|-----|
| Cross-Platform Repo | https://github.com/Foodshareclub/foodshare-app |
| iOS Repo | https://github.com/Foodshareclub/foodshare-ios |
| Web App | https://github.com/Foodshareclub/foodshare |
| Backend | https://github.com/Foodshareclub/foodshare-backend |

---

## Team

**Tarlan Isaev (organicnz)** -- Solo developer
Built with Claude Code (Opus 4.6)

---

## Judging Alignment

| Criteria | Weight | How We Address It |
|----------|--------|-------------------|
| **Impact** | 25% | Solves real food waste problem; reaches both iOS and Android users from a single codebase; demonstrates Skip Fuse at production scale |
| **Opus 4.6 Use** | 25% | Claude Code designed the architecture, built all 24 features, 29 core modules, 137 design components, and 35 Edge Functions -- primary development tool throughout |
| **Depth & Execution** | 20% | 584 files, ~198K lines, 24 features, real-time messaging, offline sync, 21-language i18n, custom design system, self-hosted backend -- genuine engineering depth |
| **Demo** | 30% | Working cross-platform app with live Supabase backend; real-time features, map discovery, chat, gamification; code walkthrough showing single codebase → two platforms |

---

## Prizes

| Prize | Award |
|-------|-------|
| 1st Place | $50,000 API Credits |
| 2nd Place | $30,000 API Credits |
| 3rd Place | $10,000 API Credits |
| Most Creative Opus 4.6 Exploration | $5,000 API Credits |
| The "Keep Thinking" Prize | $5,000 API Credits |

---

*Built with Opus 4.6 | Cerebral Valley x Anthropic Hackathon | February 2026*
