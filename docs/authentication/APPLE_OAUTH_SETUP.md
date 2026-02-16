# Apple Sign In OAuth Setup

## What You Have

- ✅ Apple Private Key: `AuthKey_DWF3P7Z437_foodshare.p8`
- ✅ Key ID: `DWF3P7Z437` (from filename)

## What You Need to Find

### 1. Team ID (10 characters)

Find in Apple Developer Account:
1. Go to https://developer.apple.com/account
2. Click "Membership" in sidebar
3. Look for "Team ID" (e.g., `A1B2C3D4E5`)

### 2. Service ID

This is your Sign in with Apple identifier:
1. Go to https://developer.apple.com/account/resources/identifiers/list/serviceId
2. Find your Sign in with Apple service
3. Copy the identifier (e.g., `com.flutterflow.foodshare.signin`)

**OR** create a new one:
1. Click "+" to create new identifier
2. Select "Services IDs"
3. Description: "Foodshare Sign in with Apple"
4. Identifier: `com.flutterflow.foodshare.signin`
5. Enable "Sign in with Apple"
6. Configure domains and callback URLs:
   - Domain: `api.foodshare.club`
   - Callback URL: `https://api.foodshare.club/auth/v1/callback`

## Generate the Secret Key

### Option 1: Python Script (Recommended)

1. Install dependencies:
   ```bash
   pip install PyJWT cryptography
   ```

2. Edit `scripts/generate_apple_secret.py`:
   - Update `TEAM_ID` with your Team ID
   - Update `SERVICE_ID` with your Service ID

3. Run the script:
   ```bash
   python3 scripts/generate_apple_secret.py
   ```

4. Copy the generated JWT token

### Option 2: Bash Script

1. Edit `scripts/generate_apple_secret.sh`:
   - Update `TEAM_ID` with your Team ID
   - Update `SERVICE_ID` with your Service ID

2. Run the script:
   ```bash
   ./scripts/generate_apple_secret.sh
   ```

3. Copy the generated JWT token

## Configure Supabase

1. Go to your Supabase dashboard
2. Navigate to Authentication → Providers
3. Find "Apple" provider
4. Configure:
   - **Enable**: ON
   - **Client ID**: Your Service ID (e.g., `com.flutterflow.foodshare.signin`)
   - **Secret Key (for OAuth)**: Paste the generated JWT token
   - **Callback URL**: Already set by Supabase

## Configure Your iOS App

### 1. Enable Sign in with Apple Capability

In Xcode:
1. Select Foodshare target
2. Go to "Signing & Capabilities"
3. Click "+ Capability"
4. Add "Sign in with Apple"

### 2. Update Info.plist

Already configured with callback URL scheme: `foodshare://oauth-callback`

### 3. Add Apple OAuth to AuthViewModel

The OAuth flow is already set up for GitHub/Google. Add Apple:

```swift
func signInWithApple() async {
    isLoading = true
    defer { isLoading = false }
    
    do {
        let url = try await supabase.auth.getOAuthSignInURL(
            provider: .apple,
            redirectTo: URL(string: "foodshare://oauth-callback")
        )
        
        let session = ASWebAuthenticationSession(
            url: url,
            callbackURLScheme: "foodshare"
        ) { [weak self] callbackURL, error in
            guard let self = self else { return }
            
            Task { @MainActor in
                self.isLoading = false
                
                if let error = error {
                    self.errorMessage = error.localizedDescription
                    return
                }
                
                guard let callbackURL = callbackURL else {
                    self.errorMessage = "No callback URL"
                    return
                }
                
                do {
                    try await self.supabase.auth.session(from: callbackURL)
                } catch {
                    self.errorMessage = error.localizedDescription
                }
            }
        }
        
        session.presentationContextProvider = ASContextProvider.shared
        session.prefersEphemeralWebBrowserSession = true
        session.start()
    } catch {
        errorMessage = error.localizedDescription
    }
}
```

## Important Notes

- ⚠️ **Secret expires every 6 months** - Set a calendar reminder
- The secret is a JWT token, not the .p8 file itself
- For native iOS apps, you can also use `ASAuthorizationAppleIDProvider` directly (no web OAuth needed)
- Keep your .p8 file secure and never commit it to git

## Troubleshooting

### "Invalid client" error
- Check that Service ID matches exactly in Supabase and Apple Developer
- Verify callback URL is configured in Apple Developer

### "Invalid JWT" error
- Regenerate the secret key
- Check that Team ID and Key ID are correct
- Ensure the .p8 file hasn't been modified

### Secret expired
- Run the generation script again
- Update the secret in Supabase dashboard
