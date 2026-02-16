# Scripts

Utility scripts for FoodShare cross-platform development and configuration.

## Available Scripts

### Development & Build

#### quick-start.sh

Quick setup and first-run helper for new developers.

```bash
./scripts/quick-start.sh
```

#### generate-bridges.sh

Generates bridging code between Swift and Skip/Android platform layers.

```bash
./scripts/generate-bridges.sh
```

#### inject_build_phase.sh

Injects the Inject hot-reload build phase script into the Xcode project for Debug builds. Targets `Darwin/FoodShare.xcodeproj`.

```bash
./scripts/inject_build_phase.sh
```

### Testing

#### run-cross-platform-tests.sh

Runs the full cross-platform test suite across iOS and Android targets.

```bash
./scripts/run-cross-platform-tests.sh
```

### Localization

#### foodshare-i18n

BFF-based localization management CLI. Supports status checks, fetching translations, validation, caching, health checks, and export.

```bash
./scripts/foodshare-i18n status
./scripts/foodshare-i18n fetch en --platform ios
./scripts/foodshare-i18n validate --verbose
./scripts/foodshare-i18n health
./scripts/foodshare-i18n export en -o translations.json
./scripts/foodshare-i18n cache status
```

Environment variables: `FOODSHARE_AUTH_TOKEN`, `FOODSHARE_PLATFORM`

#### foodshare-l10n

Enterprise localization CLI with advanced features: health checks, delta sync, benchmarks, monitoring, locale comparison, and export.

```bash
./scripts/foodshare-l10n health
./scripts/foodshare-l10n fetch --locale de --platform ios
./scripts/foodshare-l10n delta --locale en --since 20260101000000
./scripts/foodshare-l10n benchmark --iterations 10
./scripts/foodshare-l10n monitor --interval 30
./scripts/foodshare-l10n export --locale de --format json --output de.json
./scripts/foodshare-l10n compare --from en --to de
```

Environment variables: `FOODSHARE_API_URL`, `FOODSHARE_AUTH_TOKEN`

#### translate.sh

Translation helper for quick lookups and string operations.

```bash
./scripts/translate.sh
```

#### validate_localization.sh

Validates localization files for completeness and consistency across all supported languages.

```bash
./scripts/validate_localization.sh
```

### Analysis

#### summary.sh

Generates a summary of the project structure, file counts, and codebase metrics.

```bash
./scripts/summary.sh
```

### Backend Configuration

#### configure-auth-hook.sh

Configures the Supabase "Before User Created" auth hook for the geocoding edge function on the self-hosted VPS. Checks current hook status and provides manual setup instructions.

```bash
./scripts/configure-auth-hook.sh
```

See the script header comments for VPS configuration steps.

---

## CI Scripts

Additional CI/CD scripts are located in the `ci_scripts/` directory at the project root:

| Script | Purpose |
|--------|---------|
| `ci_scripts/ci_post_clone.sh` | Post-clone setup for Xcode Cloud |
| `ci_scripts/ci_pre_xcodebuild.sh` | Pre-build configuration for Xcode Cloud |
| `ci_scripts/bump-build.sh` | Increment build number |
| `ci_scripts/bump-version.sh` | Increment marketing version |
| `ci_scripts/utils/bump_version_smart.sh` | Smart version bumper (updates both Xcode project and Skip.env) |
| `ci_scripts/generate-release-notes.sh` | Generate release notes from git history |
| `ci_scripts/ios_icon_manager.sh` | Manage app icons for different build configs |
| `ci_scripts/setup-monitoring.sh` | Configure monitoring for CI builds |

---

## Related Documentation

- [CI/CD Workflows](../.github/workflows/) - GitHub Actions workflows
- [Darwin/](../Darwin/) - iOS-specific Xcode project and configuration
- [Android/](../Android/) - Android-specific Gradle project
