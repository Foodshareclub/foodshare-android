# Implementation Plan

- [ ] 1. Set up notification infrastructure and permissions
  - Create Features/Notifications directory structure with Domain/Data/Presentation layers
  - Add UserNotifications framework to project
  - Configure app capabilities for Push Notifications and Background Modes
  - _Requirements: 1.1, 2.1, 3.1_

- [ ] 2. Implement core notification models and protocols
  - [ ] 2.1 Create NotificationPayload model with category, priority, and action types
    - Define Codable struct with all notification metadata fields
    - Implement category enum (newListing, newMessage, reservationUpdate, etc.)
    - _Requirements: 1.2, 2.2, 3.2_

  - [ ] 2.2 Create NotificationPreferences model
    - Implement Codable struct with all user preference fields
    - Add isQuietTime() method to check quiet hours
    - _Requirements: 4.1, 4.5_

  - [ ] 2.3 Define repository and service protocols
    - Create NotificationRepository protocol for history storage
    - Create APNsService protocol for device token management
    - Create NotificationRouter protocol for navigation
    - _Requirements: 8.1, 1.1_

- [ ] 3. Build NotificationManager core functionality
  - [ ] 3.1 Implement authorization request flow
    - Request UNAuthorizationStatus from UNUserNotificationCenter
    - Handle authorization status changes
    - Store authorization status in @Observable property
    - _Requirements: 4.3, 4.4_

  - [ ] 3.2 Implement device token registration
    - Handle device token data from AppDelegate
    - Convert token data to hex string
    - Send token to Supabase via APNsService
    - _Requirements: 1.1_

  - [ ] 3.3 Implement notification response handling
    - Set up UNUserNotificationCenterDelegate
    - Parse notification payload from userInfo
    - Route to appropriate screen using NotificationRouter
    - _Requirements: 1.3, 2.3, 3.4_

  - [ ] 3.4 Implement local notification scheduling
    - Create UNNotificationRequest from LocalNotification model
    - Schedule with UNUserNotificationCenter
    - Handle time-based and location-based triggers
    - _Requirements: 3.5, 5.1_

  - [ ] 3.5 Implement badge count management
    - Update app icon badge count
    - Sync badge count with unread message count
    - Clear badge when notifications are read
    - _Requirements: 2.6_

- [ ] 4. Implement APNsService for Supabase integration
  - [ ] 4.1 Create SupabaseAPNsService implementation
    - Implement registerDeviceToken method with upsert to device_tokens table
    - Implement unregisterDeviceToken method
    - Implement updateNotificationPreferences method
    - _Requirements: 1.1, 4.2_

  - [ ] 4.2 Add error handling and retry logic
    - Handle network failures with exponential backoff
    - Log registration failures
    - Provide fallback to local notifications only
    - _Requirements: 1.1_

- [ ] 5. Implement NotificationRouter for navigation
  - [ ] 5.1 Create DefaultNotificationRouter implementation
    - Map notification categories to AppRoute enum values
    - Extract entity IDs from notification payload
    - Return appropriate route for each notification type
    - _Requirements: 1.3, 2.3, 3.4_

  - [ ] 5.2 Integrate router with app navigation
    - Connect router to main navigation coordinator
    - Handle deep linking from notifications
    - Ensure proper navigation stack management
    - _Requirements: 1.3, 2.3, 3.4_

- [ ] 6. Build notification history repository
  - [ ] 6.1 Implement LocalNotificationRepository
    - Set up local storage (UserDefaults or SQLite)
    - Implement saveNotification method
    - Implement fetchNotifications with limit parameter
    - _Requirements: 8.1, 8.2_

  - [ ] 6.2 Implement read/delete operations
    - Implement markAsRead method
    - Implement deleteNotification method
    - Implement clearAll method
    - Implement getUnreadCount method
    - _Requirements: 8.4, 8.5_

- [ ] 7. Create notification settings UI
  - [ ] 7.1 Build NotificationSettingsView
    - Create toggle switches for each notification category
    - Add search radius slider (1-100 km)
    - Add quiet hours time pickers
    - Bind to NotificationPreferences model
    - _Requirements: 4.1, 4.2, 4.5_

  - [ ] 7.2 Build NotificationPermissionView
    - Create educational prompt explaining notification benefits
    - Add "Enable Notifications" button
    - Add "Open Settings" deep link for denied permissions
    - Show current authorization status
    - _Requirements: 4.4_

  - [ ] 7.3 Integrate settings with preferences sync
    - Save preferences locally when changed
    - Sync preferences to Supabase via APNsService
    - Load preferences on app launch
    - _Requirements: 4.2_

- [ ] 8. Create notification history UI
  - [ ] 8.1 Build NotificationHistoryView
    - Display list of StoredNotification items
    - Show notification icon, title, body, and timestamp
    - Indicate read/unread status visually
    - Implement pull-to-refresh
    - _Requirements: 8.1, 8.3_

  - [ ] 8.2 Implement notification interaction
    - Handle tap to navigate to content
    - Add swipe-to-delete gesture
    - Add "Clear All" button
    - Update badge count when notifications are read
    - _Requirements: 8.3, 8.4, 8.5_

- [ ] 9. Set up Supabase database schema
  - [ ] 9.1 Create device_tokens table migration
    - Define table with user_id, token, platform, timestamps
    - Add unique constraint on (user_id, platform)
    - Add indexes on user_id and token
    - Enable RLS policies
    - _Requirements: 1.1_

  - [ ] 9.2 Create notification_preferences table migration
    - Define table with user_id and preferences JSONB column
    - Add unique constraint on user_id
    - Set default preferences JSON
    - Enable RLS policies
    - _Requirements: 4.1, 4.2_

  - [ ] 9.3 Create database trigger for new listings
    - Create notify_nearby_users_on_new_listing function
    - Trigger on INSERT to food_items when status is 'available'
    - Call send-notification Edge Function with listing data
    - _Requirements: 1.1_

  - [ ] 9.4 Create database trigger for new messages
    - Create notify_conversation_participants function
    - Trigger on INSERT to messages table
    - Call send-notification Edge Function with message data
    - Exclude sender from notification recipients
    - _Requirements: 2.1_

  - [ ] 9.5 Create database trigger for reservation updates
    - Create notify_reservation_status_change function
    - Trigger on UPDATE to reservations when status changes
    - Call send-notification Edge Function with reservation data
    - _Requirements: 3.1_

- [ ] 10. Implement Supabase Edge Functions
  - [ ] 10.1 Create send-notification Edge Function
    - Set up Deno function with Supabase client
    - Parse notification request payload
    - Query device_tokens based on notification type and user preferences
    - _Requirements: 1.1, 2.1, 3.1_

  - [ ] 10.2 Implement APNs integration
    - Set up APNs HTTP/2 client with auth token
    - Build APNs payload with aps dictionary
    - Send notifications to device tokens
    - Handle APNs response and invalid tokens
    - _Requirements: 1.1, 2.1, 3.1_

  - [ ] 10.3 Implement geospatial filtering for new listings
    - Create get_nearby_device_tokens database function
    - Use PostGIS to find users within search radius
    - Filter by notification preferences (newListingsEnabled)
    - Respect quiet hours settings
    - _Requirements: 1.1, 1.4, 4.5_

  - [ ] 10.4 Implement notification grouping logic
    - Track recent notifications per user
    - Group multiple new listings into single notification
    - Limit to 3+ listings within 5 minutes
    - _Requirements: 1.5_

- [ ] 11. Implement notification categories and actions
  - [ ] 11.1 Register notification categories with iOS
    - Define UNNotificationCategory for each type
    - Add UNNotificationAction for reply, view, extend, etc.
    - Register categories with UNUserNotificationCenter
    - _Requirements: 2.4, 5.2_

  - [ ] 11.2 Handle notification actions
    - Implement action handlers in NotificationManager
    - Handle "Reply" action with inline text input
    - Handle "Extend Expiry" action to open edit view
    - Handle "Mark Unavailable" action to update listing
    - _Requirements: 2.4, 5.2, 5.3_

- [ ] 12. Implement expiry warning notifications
  - [ ] 12.1 Schedule local notifications for expiring listings
    - Query user's active listings on app launch
    - Calculate time until expiry (2 hours before)
    - Schedule UNNotificationRequest with time trigger
    - _Requirements: 5.1_

  - [ ] 12.2 Cancel expiry notifications when appropriate
    - Cancel when listing is claimed or marked unavailable
    - Cancel when listing is deleted
    - Ensure only one notification per listing
    - _Requirements: 5.4, 5.5_

- [ ] 13. Implement background fetch and silent notifications
  - [ ] 13.1 Configure background modes
    - Enable "Background fetch" and "Remote notifications" capabilities
    - Implement application(_:performFetchWithCompletionHandler:)
    - Set minimum background fetch interval
    - _Requirements: 7.1, 7.2_

  - [ ] 13.2 Handle silent notifications
    - Check for content-available flag in notification payload
    - Fetch updated data without showing alert
    - Complete within 30-second timeout
    - Cache data locally for offline access
    - _Requirements: 7.2, 7.3, 7.5_

- [ ] 14. Implement notification priority and interruption levels
  - [ ] 14.1 Assign priority levels to notifications
    - Set high priority for new messages and accepted reservations
    - Set medium priority for new listings nearby
    - Set low priority for weekly summaries and tips
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

  - [ ] 14.2 Configure interruption levels
    - Use .timeSensitive for high priority
    - Use .active for medium priority
    - Use .passive for low priority
    - _Requirements: 6.2, 6.3, 6.4_

  - [ ] 14.3 Add custom sounds for notification types
    - Add sound files to project bundle
    - Assign sounds to notification categories
    - Respect system sound settings
    - _Requirements: 6.5_

- [ ] 15. Implement notification suppression logic
  - [ ] 15.1 Suppress notifications for active conversations
    - Track currently visible conversation ID
    - Check in notification handler before displaying
    - Suppress only for matching conversation
    - _Requirements: 2.5_

  - [ ] 15.2 Respect quiet hours
    - Check NotificationPreferences.isQuietTime() before sending
    - Queue non-urgent notifications for later delivery
    - Allow urgent notifications (reservation accepted) during quiet hours
    - _Requirements: 4.5_

- [ ] 16. Integrate notifications with app lifecycle
  - [ ] 16.1 Initialize NotificationManager in App
    - Create NotificationManager instance in FoodshareApp
    - Request authorization on first launch
    - Register for remote notifications
    - _Requirements: 1.1, 4.3_

  - [ ] 16.2 Handle app delegate methods
    - Implement didRegisterForRemoteNotificationsWithDeviceToken
    - Implement didFailToRegisterForRemoteNotificationsWithError
    - Implement didReceiveRemoteNotification
    - _Requirements: 1.1_

  - [ ] 16.3 Update badge count on app launch
    - Fetch unread count from repository
    - Update app icon badge
    - Clear badge when user views notifications
    - _Requirements: 2.6, 8.4_

- [ ]* 17. Write unit tests for notification system
  - Create test fixtures for NotificationPayload and NotificationPreferences
  - Test NotificationManager authorization flow
  - Test NotificationRouter routing logic
  - Test quiet hours calculation
  - Test notification grouping logic
  - _Requirements: All_

- [ ]* 18. Write integration tests
  - Test device token registration with mock Supabase
  - Test notification delivery end-to-end
  - Test background fetch triggering
  - Test notification action handling
  - _Requirements: All_
