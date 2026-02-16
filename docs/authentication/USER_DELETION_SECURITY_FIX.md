# User Deletion Security Issue

## Problem

The current `deleteAccount()` implementation in `AuthViewModel.swift` (lines 127-207) attempts to delete users by calling Supabase's `/auth/v1/user` DELETE endpoint directly from the iOS app using the anon key:

```swift
// Current implementation - INSECURE
guard var request = createAuthenticatedRequest(
    path: "/auth/v1/user",
    method: "DELETE",
    accessToken: session.accessToken
) else {
    errorMessage = "Invalid API URL"
    return
}
```

**Security Issue**: The Supabase user deletion endpoint requires the **service role key** (admin privileges), not the anon key. Shipping the service role key in the iOS app would be a critical security vulnerability, as it would allow anyone to:
- Delete any user account
- Access admin-only endpoints
- Bypass Row Level Security (RLS) policies
- Perform other privileged operations

## Solution

Create a new backend endpoint that:
1. Accepts authenticated requests from the iOS app (using user's access token)
2. Validates the user can only delete their own account
3. Uses the service role key server-side to perform the deletion
4. Returns appropriate success/error responses

### Backend Endpoint Specification

**Endpoint**: `DELETE /api/user/account`

**Headers**:
```
Authorization: Bearer {user_access_token}
```

**Response Codes**:
- `200 OK` - Account deleted successfully
- `401 Unauthorized` - Invalid or expired token
- `403 Forbidden` - User doesn't have permission
- `500 Internal Server Error` - Server-side deletion failed

**Backend Implementation** (pseudocode):
```javascript
// DELETE /api/user/account
async function deleteUserAccount(req, res) {
  // 1. Verify user's access token
  const user = await verifyAccessToken(req.headers.authorization);
  if (!user) {
    return res.status(401).json({ error: "Unauthorized" });
  }

  // 2. Use service role client to delete the user
  const { error } = await supabaseAdmin.auth.admin.deleteUser(user.id);
  
  if (error) {
    console.error("Failed to delete user:", error);
    return res.status(500).json({ error: "Failed to delete account" });
  }

  // 3. Return success
  return res.status(200).json({ message: "Account deleted successfully" });
}
```

### iOS App Changes

Update `AuthViewModel.swift` to call the new backend endpoint:

```swift
@MainActor
func deleteAccount() async {
    isLoading = true
    errorMessage = nil
    
    defer {
        isLoading = false
    }
    
    guard user != nil else {
        errorMessage = "No user logged in"
        return
    }
    
    do {
        // Get current session for authentication
        let session = try await supabase.auth.session
        
        // Call backend endpoint instead of Supabase directly
        let backendURL = ProcessInfo.processInfo.environment["BACKEND_URL"] 
            ?? "https://foodshare.app/api"
        
        guard let url = URL(string: "\(backendURL)/user/account") else {
            errorMessage = "Invalid API URL"
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue("Bearer \(session.accessToken)", forHTTPHeaderField: "Authorization")
        request.timeoutInterval = 15
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            errorMessage = "Invalid server response"
            return
        }
        
        guard (200...299).contains(httpResponse.statusCode) else {
            let responseBody = String(data: data, encoding: .utf8) ?? "No response body"
            
            switch httpResponse.statusCode {
            case 401:
                errorMessage = "Session expired. Please sign in again."
            case 403:
                errorMessage = "You don't have permission to delete this account."
            case 500...599:
                errorMessage = "Server error. Please try again later."
            default:
                errorMessage = "Failed to delete account (Error \(httpResponse.statusCode))"
            }
            return
        }
        
        // Sign out after successful deletion
        try await supabase.auth.signOut()
        user = nil
        errorMessage = nil
        
    } catch let error as URLError {
        switch error.code {
        case .timedOut:
            errorMessage = "Request timed out. Please check your connection and try again."
        case .notConnectedToInternet:
            errorMessage = "No internet connection. Please connect and try again."
        case .networkConnectionLost:
            errorMessage = "Connection lost. Please try again."
        default:
            errorMessage = "Network error: \(error.localizedDescription)"
        }
    } catch {
        errorMessage = "Failed to delete account: \(error.localizedDescription)"
    }
}
```

## Additional Considerations

### Database Cleanup

The backend endpoint should also handle:
1. **Cascade deletion** of user-related data:
   - Conversations
   - Messages
   - Subscription records
   - Any other user-specific data

2. **Foreign key constraints**: Ensure database schema has proper `ON DELETE CASCADE` rules or handle cleanup explicitly

### Testing Checklist

- [ ] Backend endpoint created and deployed
- [ ] iOS app updated to use new endpoint
- [ ] Test successful deletion flow
- [ ] Test error cases (expired token, network errors)
- [ ] Verify all user data is cleaned up from database
- [ ] Test that deleted users cannot sign in again
- [ ] Verify subscription cancellation (if applicable)

## Implementation Status

### ✅ iOS App Updated
- `AuthViewModel.swift` now calls backend endpoint `/api/user/account`
- Removed insecure `createAuthenticatedRequest()` helper
- Proper error handling for all cases

### ⏳ Backend Endpoint Required
The backend needs to implement `DELETE /api/user/account` before this feature will work.

## Security Benefits

✅ Service role key never leaves the server
✅ Users can only delete their own accounts
✅ Centralized deletion logic with proper error handling
✅ Audit trail possible on server side
✅ Can add rate limiting to prevent abuse
