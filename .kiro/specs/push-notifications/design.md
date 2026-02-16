# Push Notifications System - Design Document

## Overview

The Push Notifications System integrates Apple Push Notification service (APNs) with Supabase Edge Functions to deliver real-time alerts to Foodshare users. The system uses a combination of remote notifications (server-triggered) and local notifications (app-scheduled) to keep users informed about new listings, messages, reservations, and account activities. The architecture follows Clean Architecture principles with a dedicated Notifications feature module.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         iOS App                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Presentation Layer                                     │ │
│  │  - NotificationSettingsView                            │ │
│  │  - NotificationHistoryView                             │ │
│  │  - NotificationPermissionView                          │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Domain Layer                                          │ │
│  │  - NotificationManager (handles registration)          │ │
│  │  - NotificationRouter (handles navigation)             │ │
│  │  - NotificationPreferences (user settings)             │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Data Layer                                            │ │
│  │  - APNsService (device token registration)            │ │
│  │  - NotificationRepository (history storage)            │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ APNs Device Token
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    Supabase Backend                          │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Edge Functions                                        │ │
│  │  - send-notification (triggers push)                   │ │
│  │  - register-device (stores tokens)                     │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Database Triggers                                     │ │
│  │  - on_new_food_item → notify nearby users             │ │
│  │  - on_new_message → notify conversation participants   │ │
│  │  - on_reservation_update → notify relevant users      │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  Tables                                                │ │
│  │  - device_tokens (user_id, token, platform)           │ │
│  │  - notification_preferences (user_id, settings)        │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTP/2 Push
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                Apple Push Notification Service               │
└─────────────────────────────────────────────────────────────┘
```

### Notification Flow

1. **Registration**: App requests permission → receives device token → sends to Supabase
2. **Trigger**: Database event occurs (new listing, message, etc.)
3. **Processing**: Database trigger calls Edge Function with event data
4. **Delivery**: Edge Function sends payload to APNs with device tokens
5. **Reception**: iOS delivers notification → user taps → app routes to content

## Components and Interfaces

### 1. NotificationManager

Central coordinator for all notification operations.

```swift
@Observable
final class NotificationManager: NSObject, Sendable {
    // MARK: - Properties
    var authorizationStatus: UNAuthorizationStatus = .notDetermined
    var deviceToken: String?
    var isRegistered: Bool = false
    
    private let center = UNUserNotificationCenter.current()
    private let repository: NotificationRepository
    private let apnsService: APNsService
    
    // MARK: - Initialization
    init(repository: NotificationRepository, apnsService: APNsService) {
        self.repository = repository
        self.apnsService = apnsService
        super.init()
        center.delegate = self
    }
    
    // MARK: - Public Methods
    func requestAuthorization() async throws -> Bool
    func registerForRemoteNotifications() async throws
    func handleDeviceToken(_ token: Data) async throws
    func handleNotificationResponse(_ response: UNNotificationResponse) async
    func scheduleLocalNotification(_ notification: LocalNotification) async throws
    func cancelNotification(withIdentifier identifier: String)
    func updateBadgeCount(_ count: Int)
}
```

### 2. NotificationRouter

Handles navigation when notifications are tapped.

```swift
protocol NotificationRouter: Sendable {
    func route(for notification: NotificationPayload) -> AppRoute?
}

final class DefaultNotificationRouter: NotificationRouter {
    func route(for notification: NotificationPayload) -> AppRoute? {
        switch notification.category {
        case .newListing:
            return .listingDetail(id: notification.entityId)
        case .newMessage:
            return .conversation(id: notification.entityId)
        case .reservationUpdate:
            return .reservationDetail(id: notification.entityId)
        case .accountUpdate:
            return .profile
        }
    }
}
```

### 3. NotificationPreferences

Manages user notification settings.

```swift
struct NotificationPreferences: Codable, Sendable {
    var newListingsEnabled: Bool = true
    var messagesEnabled: Bool = true
    var reservationsEnabled: Bool = true
    var accountUpdatesEnabled: Bool = true
    var searchRadius: Double = 5.0 // kilometers
    var quietHoursEnabled: Bool = false
    var quietHoursStart: Date?
    var quietHoursEnd: Date?
    
    func isQuietTime() -> Bool {
        guard quietHoursEnabled,
              let start = quietHoursStart,
              let end = quietHoursEnd else {
            return false
        }
        
        let now = Date()
        let calendar = Calendar.current
        let nowComponents = calendar.dateComponents([.hour, .minute], from: now)
        let startComponents = calendar.dateComponents([.hour, .minute], from: start)
        let endComponents = calendar.dateComponents([.hour, .minute], from: end)
        
        // Handle overnight quiet hours
        if startComponents.hour! > endComponents.hour! {
            return nowComponents.hour! >= startComponents.hour! || 
                   nowComponents.hour! <= endComponents.hour!
        }
        
        return nowComponents.hour! >= startComponents.hour! && 
               nowComponents.hour! <= endComponents.hour!
    }
}
```

### 4. APNsService

Handles device token registration with Supabase.

```swift
protocol APNsService: Sendable {
    func registerDeviceToken(_ token: String, userId: UUID) async throws
    func unregisterDeviceToken(_ token: String) async throws
    func updateNotificationPreferences(_ preferences: NotificationPreferences, userId: UUID) async throws
}

final class SupabaseAPNsService: APNsService {
    private let supabase: SupabaseClient
    
    init(supabase: SupabaseClient) {
        self.supabase = supabase
    }
    
    func registerDeviceToken(_ token: String, userId: UUID) async throws {
        try await supabase
            .from("device_tokens")
            .upsert([
                "user_id": userId.uuidString,
                "token": token,
                "platform": "ios",
                "updated_at": ISO8601DateFormatter().string(from: Date())
            ])
            .execute()
    }
    
    func unregisterDeviceToken(_ token: String) async throws {
        try await supabase
            .from("device_tokens")
            .delete()
            .eq("token", value: token)
            .execute()
    }
    
    func updateNotificationPreferences(_ preferences: NotificationPreferences, userId: UUID) async throws {
        let encoder = JSONEncoder()
        let data = try encoder.encode(preferences)
        let json = try JSONSerialization.jsonObject(with: data) as! [String: Any]
        
        try await supabase
            .from("notification_preferences")
            .upsert([
                "user_id": userId.uuidString,
                "preferences": json
            ])
            .execute()
    }
}
```

### 5. NotificationRepository

Manages local notification history.

```swift
protocol NotificationRepository: Sendable {
    func saveNotification(_ notification: StoredNotification) async throws
    func fetchNotifications(limit: Int) async throws -> [StoredNotification]
    func markAsRead(_ notificationId: String) async throws
    func deleteNotification(_ notificationId: String) async throws
    func clearAll() async throws
    func getUnreadCount() async throws -> Int
}

final class LocalNotificationRepository: NotificationRepository {
    private let database: DatabaseService
    
    // Implementation using local SQLite or Core Data
}
```

## Data Models

### NotificationPayload

```swift
struct NotificationPayload: Codable, Sendable {
    let id: String
    let category: NotificationCategory
    let title: String
    let body: String
    let entityId: String
    let timestamp: Date
    let priority: NotificationPriority
    let imageUrl: String?
    let actions: [NotificationActionType]
    
    enum NotificationCategory: String, Codable {
        case newListing = "new_listing"
        case newMessage = "new_message"
        case reservationUpdate = "reservation_update"
        case accountUpdate = "account_update"
        case expiryWarning = "expiry_warning"
    }
    
    enum NotificationPriority: String, Codable {
        case high, medium, low
    }
    
    enum NotificationActionType: String, Codable {
        case reply, view, extend, markUnavailable, dismiss
    }
}
```

### LocalNotification

```swift
struct LocalNotification: Sendable {
    let identifier: String
    let title: String
    let body: String
    let trigger: NotificationTrigger
    let category: NotificationPayload.NotificationCategory
    let userInfo: [String: Any]
    
    enum NotificationTrigger {
        case timeInterval(TimeInterval)
        case date(Date)
        case location(CLLocationCoordinate2D, radius: Double)
    }
}
```

### StoredNotification

```swift
struct StoredNotification: Codable, Identifiable, Sendable {
    let id: String
    let payload: NotificationPayload
    let receivedAt: Date
    var isRead: Bool
    var isDeleted: Bool
}
```

## Database Schema

### device_tokens Table

```sql
CREATE TABLE device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles_foodshare(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    platform TEXT NOT NULL CHECK (platform IN ('ios', 'android')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, platform)
);

CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_token ON device_tokens(token);
```

### notification_preferences Table

```sql
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES profiles_foodshare(id) ON DELETE CASCADE UNIQUE,
    preferences JSONB NOT NULL DEFAULT '{
        "newListingsEnabled": true,
        "messagesEnabled": true,
        "reservationsEnabled": true,
        "accountUpdatesEnabled": true,
        "searchRadius": 5.0,
        "quietHoursEnabled": false
    }'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
```

### Database Triggers

```sql
-- Trigger for new food items
CREATE OR REPLACE FUNCTION notify_nearby_users_on_new_listing()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM net.http_post(
        url := current_setting('app.supabase_functions_url') || '/send-notification',
        headers := jsonb_build_object(
            'Content-Type', 'application/json',
            'Authorization', 'Bearer ' || current_setting('app.service_role_key')
        ),
        body := jsonb_build_object(
            'type', 'new_listing',
            'listing_id', NEW.id,
            'latitude', NEW.pickup_latitude,
            'longitude', NEW.pickup_longitude,
            'title', NEW.title,
            'expiry_date', NEW.expiry_date
        )
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_new_food_item
AFTER INSERT ON food_items
FOR EACH ROW
WHEN (NEW.status = 'available')
EXECUTE FUNCTION notify_nearby_users_on_new_listing();
```

## Edge Functions

### send-notification Function

```typescript
// supabase/functions/send-notification/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

interface NotificationRequest {
  type: 'new_listing' | 'new_message' | 'reservation_update'
  listing_id?: string
  message_id?: string
  reservation_id?: string
  latitude?: number
  longitude?: number
  title?: string
  body?: string
}

serve(async (req) => {
  const supabase = createClient(
    Deno.env.get('SUPABASE_URL')!,
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!
  )
  
  const payload: NotificationRequest = await req.json()
  
  // Get relevant device tokens based on notification type
  let deviceTokens: string[] = []
  
  if (payload.type === 'new_listing') {
    // Find users within radius who have new listings enabled
    const { data: tokens } = await supabase.rpc('get_nearby_device_tokens', {
      lat: payload.latitude,
      long: payload.longitude,
      notification_type: 'newListingsEnabled'
    })
    deviceTokens = tokens?.map(t => t.token) || []
  }
  
  // Send to APNs
  const apnsPayload = {
    aps: {
      alert: {
        title: payload.title,
        body: payload.body
      },
      badge: 1,
      sound: 'default',
      'interruption-level': 'active',
      'relevance-score': 0.8
    },
    category: payload.type,
    entityId: payload.listing_id || payload.message_id || payload.reservation_id
  }
  
  // Send via APNs HTTP/2 API
  const results = await Promise.allSettled(
    deviceTokens.map(token => sendToAPNs(token, apnsPayload))
  )
  
  return new Response(JSON.stringify({ sent: results.length }), {
    headers: { 'Content-Type': 'application/json' }
  })
})
```

## Error Handling

### Permission Denied

```swift
func handlePermissionDenied() {
    // Show in-app prompt explaining benefits
    showPermissionEducation()
    
    // Provide deep link to Settings
    if let url = URL(string: UIApplication.openSettingsURLString) {
        UIApplication.shared.open(url)
    }
}
```

### Token Registration Failure

```swift
func handleRegistrationFailure(_ error: Error) async {
    logger.error("Failed to register device token: \(error)")
    
    // Retry with exponential backoff
    let retryDelays: [TimeInterval] = [1, 5, 15, 60]
    for delay in retryDelays {
        try? await Task.sleep(nanoseconds: UInt64(delay * 1_000_000_000))
        do {
            try await registerForRemoteNotifications()
            return
        } catch {
            continue
        }
    }
    
    // After all retries, fall back to local notifications only
    isRegistered = false
}
```

### Background Fetch Timeout

```swift
func handleBackgroundFetch(completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
    Task {
        do {
            let hasNewData = try await fetchUpdates()
            completionHandler(hasNewData ? .newData : .noData)
        } catch {
            completionHandler(.failed)
        }
    }
    
    // Ensure completion within 30 seconds
    DispatchQueue.main.asyncAfter(deadline: .now() + 29) {
        completionHandler(.failed)
    }
}
```

## Testing Strategy

### Unit Tests

- NotificationManager authorization flow
- NotificationRouter routing logic
- NotificationPreferences quiet hours calculation
- Payload parsing and validation

### Integration Tests

- Device token registration with Supabase
- Notification delivery end-to-end
- Background fetch triggering
- Notification action handling

### UI Tests

- Permission request flow
- Settings screen interactions
- Notification history display
- Tapping notifications and navigation

## Performance Considerations

1. **Battery Efficiency**: Use silent notifications sparingly, batch updates
2. **Network Usage**: Compress payloads, use efficient JSON encoding
3. **Storage**: Limit notification history to 30 days, implement cleanup
4. **Background Time**: Complete background fetches within 30 seconds
5. **Token Management**: Cache device token locally, update only when changed

## Security Considerations

1. **Token Storage**: Store device tokens securely in Supabase with RLS
2. **Payload Validation**: Validate all notification payloads before processing
3. **User Privacy**: Don't include sensitive data in notification body
4. **Authentication**: Require authenticated requests to Edge Functions
5. **Rate Limiting**: Implement rate limits on notification sending

## Accessibility

1. **VoiceOver**: Ensure notification content is readable by screen readers
2. **Dynamic Type**: Support user font size preferences in notification UI
3. **Reduce Motion**: Respect motion preferences for notification animations
4. **Sound**: Provide distinct sounds for different notification types
5. **Haptics**: Use appropriate haptic feedback for notification actions
