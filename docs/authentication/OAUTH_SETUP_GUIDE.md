# OAuth Providers Setup Guide

## Overview
Foodshare now supports four OAuth providers:
- 🍎 **Apple Sign In** (Native iOS)
- 🔵 **Google** 
- ⚫ **GitHub**
- 🏠 **Nextdoor** (OAuth 2.0 + OpenID Connect) 🆕

## Xcode Configuration

### 1. Add Sign in with Apple Capability

1. Open `Foodshare.xcodeproj` in Xcode
2. Select the **Foodshare** target
3. Go to **Signing & Capabilities** tab
4. Click **+ Capability**
5. Add **Sign in with Apple**
6. Ensure the entitlements file is linked: `Foodshare/Foodshare/Foodshare.entitlements`

### 2. Verify Entitlements File

The entitlements file should contain:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>com.apple.developer.applesignin</key>
	<array>
		<string>Default</string>
	</array>
</dict>
</plist>
```

### 3. Link Entitlements in Project Settings

1. Select **Foodshare** target
2. Go to **Build Settings**
3. Search for "Code Signing Entitlements"
4. Set value to: `Foodshare/Foodshare.entitlements`

## Supabase Dashboard Configuration

### Apple Sign In Setup

1. Go to your Supabase Dashboard
2. Navigate to **Authentication** → **Providers**
3. Find **Apple** and click **Enable**
4. Configure:
   - **Enabled**: Toggle ON
   - **Client ID (Services ID)**: Your Apple Services ID (e.g., `com.flutterflow.foodshare.signin`)
   - **Secret Key**: Your Apple Sign In private key
   - **Redirect URL**: `https://api.foodshare.club/auth/v1/callback`

#### Getting Apple Credentials

1. Go to [Apple Developer Portal](https://developer.apple.com/account/)
2. Navigate to **Certificates, Identifiers & Profiles**
3. Create a **Services ID**:
   - Identifier: `com.flutterflow.foodshare.signin`
   - Enable "Sign in with Apple"
   - Configure domains and redirect URLs
4. Create a **Key** for Sign in with Apple:
   - Download the `.p8` key file
   - Note the Key ID
   - Note your Team ID
5. Use these to generate the secret key for Supabase

### Google Sign In Setup

1. In Supabase Dashboard → **Authentication** → **Providers**
2. Find **Google** and click **Enable**
3. Configure:
   - **Enabled**: Toggle ON
   - **Client ID**: Your Google OAuth Client ID
   - **Client Secret**: Your Google OAuth Client Secret
   - **Redirect URL**: `https://api.foodshare.club/auth/v1/callback`

#### Getting Google Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Navigate to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth client ID**
5. Choose **iOS** application type
6. Configure:
   - Bundle ID: `com.flutterflow.foodshare`
   - Add authorized redirect URIs:
     - `https://api.foodshare.club/auth/v1/callback`
     - `foodshare://oauth-callback`
7. Copy the Client ID and Client Secret

### GitHub Sign In Setup

1. In Supabase Dashboard → **Authentication** → **Providers**
2. Find **GitHub** and click **Enable**
3. Configure:
   - **Enabled**: Toggle ON
   - **Client ID**: Your GitHub OAuth App Client ID
   - **Client Secret**: Your GitHub OAuth App Client Secret
   - **Redirect URL**: `https://api.foodshare.club/auth/v1/callback`

#### Getting GitHub Credentials

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Configure:
   - Application name: `Foodshare`
   - Homepage URL: `https://foodshare.app`
   - Authorization callback URL: `https://api.foodshare.club/auth/v1/callback`
4. Click **Register application**
5. Copy the Client ID
6. Generate a new Client Secret and copy it

### Nextdoor Sign In Setup 🆕

Nextdoor uses OAuth 2.0 + OpenID Connect. The token exchange happens via a Supabase Edge Function to keep the client secret secure.

#### Getting Nextdoor Credentials

1. Go to [Nextdoor Developer Portal](https://developer.nextdoor.com/)
2. Create a new application
3. Configure OAuth settings:
   - **Redirect URI**: `foodshare://oauth-callback`
   - **Scopes**: `openid`, `profile:read`
4. Copy the **Client ID** and **Client Secret**

#### Environment Configuration

Add to your environment variables:
- `NEXTDOOR_CLIENT_ID`: Your Nextdoor OAuth Client ID

The client secret should be stored in Supabase Vault (not in the app):
- `NEXTDOOR_CLIENT_SECRET`: Your Nextdoor OAuth Client Secret

#### Edge Function Setup

Deploy the `nextdoor-token-exchange` Edge Function to handle the authorization code exchange:

```typescript
// supabase/functions/nextdoor-token-exchange/index.ts
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

Deno.serve(async (req: Request) => {
  const { code, redirect_uri, nonce } = await req.json();
  
  const clientId = Deno.env.get("NEXTDOOR_CLIENT_ID");
  const clientSecret = Deno.env.get("NEXTDOOR_CLIENT_SECRET");
  
  const tokenResponse = await fetch("https://www.nextdoor.com/v3/token/", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      code,
      redirect_uri,
      client_id: clientId!,
      client_secret: clientSecret!,
    }),
  });
  
  const tokens = await tokenResponse.json();
  
  return new Response(JSON.stringify({
    idToken: tokens.id_token,
    accessToken: tokens.access_token,
    name: tokens.name,
    email: tokens.email,
  }), {
    headers: { "Content-Type": "application/json" },
  });
});
```

#### Swift Model Required

Add the `NextdoorTokenResponse` model to your codebase:

```swift
struct NextdoorTokenResponse: Codable {
    let idToken: String
    let accessToken: String
    let name: String?
    let email: String?
    
    enum CodingKeys: String, CodingKey {
        case idToken = "id_token"
        case accessToken = "access_token"
        case name
        case email
    }
}
```

#### AppEnvironment Configuration

Add to `AppEnvironment.swift`:

```swift
static var nextdoorClientId: String? {
    ProcessInfo.processInfo.environment["NEXTDOOR_CLIENT_ID"]
}
```

## URL Schemes Configuration

The app is already configured with the custom URL scheme in `Info.plist`:

```xml
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>foodshare</string>
        </array>
    </dict>
</array>
```

This allows the app to handle OAuth callbacks:
- `foodshare://oauth-callback` - OAuth provider callbacks
- `foodshare://email-confirmed` - Email verification

## Testing OAuth Providers

### Test Apple Sign In
1. Run app on physical device (Simulator may have limitations)
2. Tap "Continue with Apple"
3. Sign in with your Apple ID
4. Verify successful authentication

### Test Google Sign In
1. Run app on device or simulator
2. Tap "Continue with Google"
3. Sign in with your Google account
4. Verify successful authentication

### Test GitHub Sign In
1. Run app on device or simulator
2. Tap "Continue with GitHub"
3. Sign in with your GitHub account
4. Verify successful authentication

### Test Nextdoor Sign In 🆕
1. Run app on device or simulator
2. Tap "Continue with Nextdoor"
3. Sign in with your Nextdoor account
4. Verify successful authentication
5. Check that profile data (name, email) is populated

## Troubleshooting

### Apple Sign In Issues

**Error: "Invalid client"**
- Verify Services ID matches in Apple Developer Portal and Supabase
- Ensure Sign in with Apple capability is enabled in Xcode
- Check that entitlements file is properly linked

**Error: "User cancelled"**
- This is normal when user cancels - app handles it gracefully

### Google Sign In Issues

**Error: "Invalid client"**
- Verify Client ID and Secret in Supabase match Google Cloud Console
- Check redirect URIs are correctly configured
- Ensure OAuth consent screen is configured

**Error: "Redirect URI mismatch"**
- Add `foodshare://oauth-callback` to authorized redirect URIs in Google Cloud Console
- Add Supabase callback URL to authorized redirect URIs

### GitHub Sign In Issues

**Error: "Incorrect client credentials"**
- Verify Client ID and Secret in Supabase match GitHub OAuth App
- Regenerate Client Secret if needed

**Error: "Redirect URI mismatch"**
- Ensure callback URL in GitHub OAuth App matches Supabase callback URL

### Nextdoor Sign In Issues 🆕

**Error: "Nextdoor is not configured"**
- Ensure `NEXTDOOR_CLIENT_ID` environment variable is set
- Check that the value matches your Nextdoor Developer Portal

**Error: "No authorization code in Nextdoor callback"**
- Verify redirect URI matches exactly: `foodshare://oauth-callback`
- Check Nextdoor app configuration in Developer Portal

**Error: "Token exchange failed"**
- Verify the `nextdoor-token-exchange` Edge Function is deployed
- Check that `NEXTDOOR_CLIENT_SECRET` is set in Supabase Vault
- Review Edge Function logs for detailed error messages

**Error: "Invalid nonce"**
- This indicates a security issue - the nonce from authorization doesn't match
- Try signing in again with a fresh session

### General OAuth Issues

**Error: "No callback URL received"**
- Check URL scheme is properly configured in Info.plist
- Verify `foodshare://` scheme is registered

**Error: "Failed to start authentication session"**
- Check network connectivity
- Verify OAuth provider is enabled in Supabase
- Check Supabase credentials are correct

## Security Considerations

### Production Checklist
- [ ] Use different OAuth credentials for development and production
- [ ] Rotate secrets regularly
- [ ] Enable only necessary OAuth providers
- [ ] Configure proper redirect URLs
- [ ] Test all OAuth flows on physical devices
- [ ] Monitor OAuth usage in Supabase dashboard
- [ ] Set up proper error logging

### Privacy
- Apple Sign In provides "Hide My Email" option - handle this gracefully
- Request only necessary scopes from OAuth providers
- Display privacy policy before OAuth sign-in
- Handle user data according to provider requirements

## Provider Comparison

| Feature | Apple | Google | GitHub | Nextdoor |
|---------|-------|--------|--------|----------|
| Native iOS | ✅ Yes | ❌ No | ❌ No | ❌ No |
| Email hiding | ✅ Yes | ❌ No | ❌ No | ❌ No |
| Required for App Store | ⚠️ If other social logins | ❌ No | ❌ No | ❌ No |
| Setup complexity | 🟡 Medium | 🟡 Medium | 🟢 Easy | 🟡 Medium |
| User base | 🍎 iOS users | 🌍 Everyone | 👨‍💻 Developers | 🏠 Neighbors |
| Edge Function required | ❌ No | ❌ No | ❌ No | ✅ Yes |
| OpenID Connect | ❌ No | ✅ Yes | ❌ No | ✅ Yes |

## App Store Requirements

⚠️ **Important**: If you offer Google or GitHub sign-in, Apple requires you to also offer Apple Sign In.

From Apple's guidelines:
> Apps that use a third-party or social login service to set up or authenticate the user's primary account with the app must also offer Sign in with Apple as an equivalent option.

This is why we've added Apple Sign In to the app.

## Next Steps

1. ✅ Add Sign in with Apple capability in Xcode
2. ✅ Link entitlements file
3. ⬜ Configure Apple Sign In in Supabase Dashboard
4. ⬜ Configure Google Sign In in Supabase Dashboard  
5. ⬜ Configure GitHub Sign In in Supabase Dashboard
6. ⬜ Configure Nextdoor Sign In:
   - ⬜ Create Nextdoor Developer app
   - ⬜ Add `NEXTDOOR_CLIENT_ID` to environment
   - ⬜ Add `NEXTDOOR_CLIENT_SECRET` to Supabase Vault
   - ⬜ Deploy `nextdoor-token-exchange` Edge Function
   - ⬜ Add `NextdoorTokenResponse` model to codebase
   - ⬜ Add `nextdoorClientId` to `AppEnvironment`
7. ⬜ Test all four OAuth providers
8. ⬜ Update privacy policy to mention OAuth providers
9. ⬜ Submit to App Store

## Support

For issues with:
- **Apple Sign In**: Check Apple Developer Portal and Xcode capabilities
- **Google Sign In**: Check Google Cloud Console OAuth configuration
- **GitHub Sign In**: Check GitHub OAuth App settings
- **Nextdoor Sign In**: Check Nextdoor Developer Portal and Edge Function logs
- **Supabase**: Check Supabase Dashboard provider configuration

All OAuth providers use the same code flow in `AuthenticationService.swift`, so if one works, the others should work too once properly configured.
