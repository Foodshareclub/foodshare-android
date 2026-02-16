# Authentication Documentation

Documentation for authentication flows, OAuth providers, and account management.

## Current Documents

### OAuth Implementation
- **OAUTH_SETUP_GUIDE.md** - Complete guide for configuring OAuth providers
- **OAUTH_TESTING_GUIDE.md** - How to test OAuth flows locally and in production
- **OAUTH_IMPLEMENTATION_COMPLETE.md** - Summary of completed OAuth implementation
- **OAUTH_PROVIDERS_SUMMARY.md** - Overview of supported OAuth providers
- **OAUTH_READY_SUMMARY.md** - Production readiness checklist for OAuth

### Apple Sign In
- **APPLE_OAUTH_SETUP.md** - Apple-specific OAuth configuration
- **APPLE_SIGNIN_SETUP.md** - Initial Apple Sign In setup
- **APPLE_SIGNIN_NATIVE.md** - Native Apple Sign In implementation
- **APPLE_SIGNIN_CHECKLIST.md** - Apple Sign In verification checklist
- **APPLE_SIGNIN_COMPLETE.md** - Completion summary
- **APPLE_SIGNIN_FINAL_STEPS.md** - Final implementation steps
- **APPLE_SIGNIN_FIX.md** - Common Apple Sign In issues
- **APPLE_SIGNIN_FIX.txt** - Additional fix notes

### Account Management
- **DELETE_ACCOUNT_IMPLEMENTATION.md** - Account deletion feature implementation
- **DELETE_ACCOUNT_TEST.md** - Testing account deletion
- **USER_DELETION_SECURITY_FIX.md** - Security improvements for account deletion

## Supported Authentication Methods

1. **Email/Password** - Traditional email authentication via Supabase
2. **Apple Sign In** - Native Apple authentication via `AppState.signInWithApple()`
3. **Google OAuth** - OAuth 2.0 via Google via `AppState.signInWithGoogle()`
4. **GitHub OAuth** - OAuth 2.0 via GitHub (available through repository)
5. **Nextdoor OAuth** - OAuth 2.0 + OpenID Connect via Nextdoor via `AuthenticationService.signInWithNextdoor()` 🆕

## Quick Start

To set up OAuth:
1. Read OAUTH_SETUP_GUIDE.md
2. Configure providers in Supabase dashboard
3. Update redirect URLs in Info.plist
4. Test using OAUTH_TESTING_GUIDE.md

## Security Notes

- All OAuth flows use PKCE (Proof Key for Code Exchange)
- Tokens are stored securely in Supabase session
- Account deletion is permanent and irreversible
- See USER_DELETION_SECURITY_FIX.md for security best practices

## Auth Hooks

### Geolocate User (Before User Created)

The `geolocate-user` edge function runs as a "Before User Created" auth hook to capture approximate user location at signup via IP geolocation.

**Configuration**:
```bash
# Set your Supabase access token
export SUPABASE_ACCESS_TOKEN="your-access-token"

# Run the configuration script
./scripts/configure-auth-hook.sh
```

The script will attempt to configure the hook via the Supabase Management API and verify the configuration. If API configuration fails (some hook types require manual setup), the script provides step-by-step instructions for manual configuration in the Supabase Dashboard.

**Manual Setup** (if needed):
1. Go to: https://studio.foodshare.club/project/default/auth/hooks
2. Find "Before User Created" hook
3. Select "HTTP" as the hook type
4. Set URI and secret as shown in the script output
5. Click "Save"

See `supabase/functions/README.md` for full details on the geolocate-user function.
