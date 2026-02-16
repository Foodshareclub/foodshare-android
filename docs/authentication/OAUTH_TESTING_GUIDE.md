# OAuth Testing Guide

## ✅ Provider Status

| Provider | Supabase Config | Code Implementation | Xcode Capability | Ready to Test |
|----------|----------------|---------------------|------------------|---------------|
| 🍎 Apple | ✅ Configured | ✅ Implemented | ⚠️ Need to add | Almost |
| 🔵 Google | ✅ Configured | ✅ Implemented | ✅ N/A | ✅ Ready |
| ⚫ GitHub | ⚠️ Need config | ✅ Implemented | ✅ N/A | After config |

## Quick Setup Remaining

### 1. Add Apple Sign In Capability (Required)

**In Xcode:**
1. Open `Foodshare.xcodeproj`
2. Select **Foodshare** target
3. Go to **Signing & Capabilities** tab
4. Click **+ Capability** button
5. Search for and add **Sign in with Apple**
6. Verify entitlements file is linked: `Foodshare/Foodshare.entitlements`

**Verify in Build Settings:**
- Search for "Code Signing Entitlements"
- Should be set to: `Foodshare/Foodshare.entitlements`

### 2. Configure GitHub in Supabase (Optional)

If you want to enable GitHub OAuth:

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Fill in:
   - **Application name**: Foodshare
   - **Homepage URL**: `https://foodshare.app`
   - **Authorization callback URL**: `https://api.foodshare.club/auth/v1/callback`
4. Click **Register application**
5. Copy **Client ID** and generate **Client Secret**
6. In Supabase Dashboard:
   - Go to Authentication → Providers → GitHub
   - Enable provider
   - Paste Client ID and Client Secret
   - Save

## Testing Checklist

### Test Apple Sign In ✅

**Requirements:**
- Physical iOS device (Simulator may have limitations)
- Apple ID signed in on device
- Sign in with Apple capability added in Xcode

**Steps:**
1. Build and run on physical device
2. Tap "Continue with Apple"
3. Apple Sign In modal should appear
4. Sign in with your Apple ID
5. Choose to share or hide email
6. Tap "Continue"
7. App should receive callback and create session
8. Verify you're signed in ✅

**Expected Result:**
- Smooth authentication flow
- Session created in Supabase
- User redirected to main app

**Common Issues:**
- "Invalid client" → Check Services ID in Apple Developer Portal matches Supabase
- "No callback" → Verify entitlements file is linked
- Capability error → Add Sign in with Apple capability in Xcode

### Test Google Sign In ✅

**Requirements:**
- Device or Simulator
- Google account

**Steps:**
1. Build and run
2. Tap "Continue with Google"
3. Google Sign In page opens in browser
4. Sign in with your Google account
5. Grant permissions
6. App should receive callback and create session
7. Verify you're signed in ✅

**Expected Result:**
- Browser opens with Google login
- After authentication, returns to app
- Session created successfully

**Common Issues:**
- "Invalid client" → Check Client ID in Supabase matches Google Cloud Console
- "Redirect URI mismatch" → Verify redirect URIs in Google Cloud Console

### Test GitHub Sign In (After Configuration)

**Requirements:**
- Device or Simulator
- GitHub account
- GitHub OAuth App configured

**Steps:**
1. Build and run
2. Tap "Continue with GitHub"
3. GitHub authorization page opens
4. Sign in with your GitHub account
5. Authorize the app
6. App should receive callback and create session
7. Verify you're signed in ✅

**Expected Result:**
- Browser opens with GitHub login
- After authorization, returns to app
- Session created successfully

## Testing Scenarios

### Scenario 1: New User Sign Up
1. Use OAuth provider (Apple/Google/GitHub)
2. First time signing in
3. Account should be created in Supabase
4. User should be signed in immediately
5. Check Supabase Dashboard → Authentication → Users

### Scenario 2: Existing User Sign In
1. Use same OAuth provider as before
2. Should recognize existing account
3. Sign in immediately
4. No duplicate accounts created

### Scenario 3: User Cancellation
1. Tap OAuth button
2. Cancel the authentication modal
3. App should handle gracefully
4. No crash, no error message (or "User cancelled")
5. User remains on login screen

### Scenario 4: Network Error
1. Turn off WiFi/cellular
2. Tap OAuth button
3. Should show appropriate error message
4. App should not crash

### Scenario 5: Multiple Providers
1. Sign in with Apple
2. Sign out
3. Sign in with Google
4. Should work independently
5. May create separate accounts (expected)

## Debugging OAuth Issues

### Enable Detailed Logging

In `AuthViewModel.swift`, the OAuth flow already has logging:
```swift
print("✅ Got SDP answer from backend")
print("Failed to initialize OAuth: \(error.localizedDescription)")
```

### Check Supabase Logs

1. Go to Supabase Dashboard
2. Navigate to Logs → Auth Logs
3. Filter by timestamp
4. Look for OAuth-related events

### Common Error Messages

| Error | Cause | Solution |
|-------|-------|----------|
| "Invalid client" | Credentials mismatch | Verify Client ID/Secret in Supabase |
| "No callback URL received" | URL scheme issue | Check Info.plist has `foodshare://` |
| "Failed to start authentication session" | Network/config issue | Check internet, verify provider enabled |
| "OAuth authentication failed" | Provider rejected | Check provider configuration |
| User cancelled | User tapped Cancel | Normal behavior, no action needed |

## Verification After Testing

### Check Supabase Dashboard

1. Go to Authentication → Users
2. Verify new users appear
3. Check provider column shows correct OAuth provider
4. Verify email addresses are populated

### Check App Behavior

- [ ] User stays signed in after app restart
- [ ] Sign out works correctly
- [ ] Can sign in again with same provider
- [ ] No crashes during OAuth flow
- [ ] Error messages are user-friendly

## Performance Testing

### Test on Multiple Devices

- [ ] iPhone (iOS 17.0+)
- [ ] iPad (iOS 17.0+)
- [ ] Different iOS versions (17.0, 17.5, 18.0+)

### Test Network Conditions

- [ ] WiFi
- [ ] Cellular (4G/5G)
- [ ] Slow network
- [ ] No network (should show error)

## Security Verification

### Check OAuth Flow Security

- [ ] Uses `ASWebAuthenticationSession` (secure)
- [ ] No credentials stored in app
- [ ] Tokens managed by Supabase
- [ ] HTTPS for all OAuth URLs
- [ ] Proper error handling (no sensitive data in errors)

### Privacy Considerations

- [ ] Apple "Hide My Email" works correctly
- [ ] User can see what data is shared
- [ ] Privacy policy accessible
- [ ] Terms of service accessible

## Ready for Submission Checklist

Before submitting to App Store:

- [ ] Apple Sign In capability added in Xcode
- [ ] Tested Apple Sign In on physical device
- [ ] Tested Google Sign In
- [ ] Tested GitHub Sign In (if enabled)
- [ ] All OAuth flows work without crashes
- [ ] User cancellation handled gracefully
- [ ] Error messages are user-friendly
- [ ] No hardcoded credentials in code
- [ ] Privacy policy mentions OAuth providers
- [ ] Screenshots show OAuth buttons

## Quick Test Script

Run through this in 5 minutes:

1. **Apple Sign In:**
   - Tap button → Sign in → Verify success ✅

2. **Sign Out:**
   - Go to Settings → Sign Out ✅

3. **Google Sign In:**
   - Tap button → Sign in → Verify success ✅

4. **Sign Out:**
   - Go to Settings → Sign Out ✅

5. **Cancellation:**
   - Tap Apple button → Cancel → No crash ✅

6. **Done!** All OAuth providers working ✅

## Next Steps

1. ✅ Apple and Google configured in Supabase
2. ✅ Code implemented for all three providers
3. ⚠️ **Add Sign in with Apple capability in Xcode** ← DO THIS NOW
4. ⬜ Test Apple Sign In on physical device
5. ⬜ Test Google Sign In
6. ⬜ (Optional) Configure and test GitHub
7. ⬜ Submit to App Store

---

**Current Status**: Ready to test after adding Apple Sign In capability in Xcode!
