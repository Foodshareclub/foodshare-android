---
name: foodshare-deployment
description: Automate build, test, and deployment workflows for Foodshare iOS. Use for release preparation, CI/CD setup, TestFlight uploads, and App Store submission.
---

<objective>
Every deployment should be boring. Automated, tested, and predictable. If your heart rate increases during deployment, you need better automation.
</objective>

<essential_principles>
## Project Configuration

- **Bundle ID**: `com.flutterflow.foodshare`
- **Scheme**: `Foodshare`
- **Platform**: iOS 17.0+
- **CI/CD**: Xcode Cloud
- **Distribution**: TestFlight → App Store

## Pre-Deployment Checklist (Non-Negotiable)

Before ANY deployment:
- [ ] All tests pass
- [ ] SwiftLint clean (no warnings)
- [ ] No force unwraps in production code
- [ ] No hardcoded secrets
- [ ] Environment variables configured
- [ ] Supabase migrations applied
- [ ] Version number bumped

## Security Checks

```bash
# Check for exposed secrets
git grep -i "sk-" -- "*.swift"
git grep -i "secret" -- "*.swift"
git grep -i "password" -- "*.swift"

# Verify .env not in git
git ls-files .env && echo "❌ .env is tracked!" || echo "✅ Safe"

# Check for print statements (use Logger)
git grep "print(" -- "*.swift" | grep -v "Tests"
```

## Version Management

```bash
# Bump build number
agvtool new-version -all 42

# Bump marketing version
agvtool new-marketing-version 1.0.0

# Check current version
agvtool what-version
agvtool what-marketing-version
```
</essential_principles>

<intake>
What deployment task do you need help with?

1. **TestFlight release** - Deploy to beta testers
2. **App Store submission** - Production release
3. **CI/CD setup** - Configure Xcode Cloud
4. **Troubleshoot build** - Fix build/archive issues
5. **Hotfix** - Emergency production fix
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "testflight", "beta", "testers" | workflows/testflight-release.md |
| 2, "app store", "production", "release" | workflows/app-store-submission.md |
| 3, "ci", "cd", "xcode cloud", "setup" | workflows/ci-cd-setup.md |
| 4, "build", "archive", "error", "troubleshoot" | workflows/troubleshoot-build.md |
| 5, "hotfix", "emergency", "urgent" | workflows/hotfix.md |
</routing>

<quick_reference>
## Common Commands

```bash
# Build for simulator
xcodebuild build \
  -scheme Foodshare \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max'

# Build for device
xcodebuild build \
  -scheme Foodshare \
  -destination 'generic/platform=iOS'

# Run tests
xcodebuild test \
  -scheme Foodshare \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max'

# Create archive
xcodebuild archive \
  -scheme Foodshare \
  -destination 'generic/platform=iOS' \
  -archivePath build/Foodshare.xcarchive

# Export IPA
xcodebuild -exportArchive \
  -archivePath build/Foodshare.xcarchive \
  -exportPath build \
  -exportOptionsPlist ExportOptions.plist
```

## Xcode Cloud Environment

```bash
# ci_scripts/ci_post_clone.sh
#!/bin/bash
set -euo pipefail

# Install dependencies
brew install swiftlint

# Set up environment
echo "SUPABASE_URL=$SUPABASE_URL" >> .env
echo "SUPABASE_PUBLISHABLE_KEY=$SUPABASE_PUBLISHABLE_KEY" >> .env

# Resolve packages
xcodebuild -resolvePackageDependencies
```

## TestFlight Upload

```bash
xcrun altool --upload-app \
  --type ios \
  --file build/Foodshare.ipa \
  --username "your@email.com" \
  --password "@keychain:AC_PASSWORD"
```

## Performance Benchmarks

Before each release:
- App launch: < 2 seconds
- Feed load: < 1 second (with cache)
- Search latency: < 500ms
- Memory idle: < 150MB
- Battery: < 5%/hour idle
</quick_reference>

<success_criteria>
Deployment is ready when:
- [ ] All tests pass (70%+ coverage)
- [ ] No SwiftLint warnings
- [ ] No force unwraps outside tests
- [ ] No secrets in codebase
- [ ] Version number incremented
- [ ] Changelog updated
- [ ] Screenshots current (if UI changed)
- [ ] Privacy policy URL valid
</success_criteria>
