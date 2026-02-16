---
inclusion: always
---

# Database Integration Guidelines

## Supabase Schema

### Foodshare iOS Tables

The iOS app uses these cross-platform tables (shared with web app):

- **profiles** - Core user profile information (identity, settings, preferences)
- **profile_stats** - User statistics and counters (ratings, items shared/received, likes)
- **profiles_with_stats** - View joining profiles + profile_stats for unified access
- **posts** - Food listings with location, images, pickup details, and arrangement status
- **rooms** - Chat conversations between food donors and recipients
- **room_participants** - Individual chat messages with timestamps
- **reviews** - User ratings and reviews for posts, forums, and challenges

**Important**: User statistics are stored in `profile_stats` table, not in `profiles`. Use the `profiles_with_stats` view for queries that need both profile data and statistics.

### Key Schema Patterns

**Geography Types**:
```sql
-- All location fields use PostGIS geography type
location geography(Point, 4326)
latitude double precision CHECK (latitude >= -90 AND latitude <= 90)
longitude double precision CHECK (longitude >= -180 AND longitude <= 180)
```

**Post Type (Category)**:
```sql
-- posts.post_type values (matches ListingCategory enum, singular form)
post_type text  -- One of: food, thing, borrow, wanted, foodbank, fridge,
                -- zerowaste, vegan, organisation, volunteer, challenge, forum
```

See `Core/Models/ListingCategory.swift` for the Swift enum with display names, icons, and colors.

**Status Fields**:
```sql
-- posts table
is_active boolean DEFAULT true  -- Post is active/visible (uses is_ prefix)
is_arranged boolean DEFAULT false  -- Post has been arranged for pickup (uses is_ prefix)
post_arranged_to uuid  -- User who arranged the pickup
post_arranged_at timestamp with time zone  -- When arrangement was made

-- Derived status in Swift:
-- available: is_active=true, is_arranged=false
-- arranged: is_active=true, is_arranged=true
-- inactive: is_active=false
```

**Timestamps**:
- `profiles` table uses `created_time` and `last_seen_at` for user activity tracking
- `posts` table uses `created_at` and `updated_at` (auto-managed)
- Use `timestamp with time zone` for all datetime fields
- Default to `now()` for creation timestamps

## Swift Integration

### Model Mapping

Map database columns to Swift models using Codable:

```swift
struct FoodListing: Codable, Identifiable {
    let id: Int
    let profileId: UUID
    let postName: String
    let postDescription: String?
    let postType: String
    let pickupTime: String?
    let availableHours: String?
    let postAddress: String?
    let latitude: Double?
    let longitude: Double?
    let gifUrl: String?
    let gifUrl2: String?
    let gifUrl3: String?
    var isActive: Bool
    var isArranged: Bool
    var postArrangedTo: UUID?
    var postArrangedAt: Date?
    var postViews: Int
    var postLikeCounter: Int
    let createdAt: Date
    let updatedAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id
        case profileId = "profile_id"
        case postName = "post_name"
        case postDescription = "post_description"
        case postType = "post_type"
        case pickupTime = "pickup_time"
        case availableHours = "available_hours"
        case postAddress = "post_address"
        case latitude, longitude
        case imageUrl1 = "image_url_1"
        case imageUrl2 = "image_url_2"
        case imageUrl3 = "image_url_3"
        case isActive = "is_active"
        case isArranged = "is_arranged"
        case postArrangedTo = "post_arranged_to"
        case postArrangedAt = "post_arranged_at"
        case postViews = "post_views"
        case postLikeCounter = "post_like_counter"
        case createdAt = "created_at"
        case updatedAt = "updated_at"
    }
}
```

### Geospatial Queries

Use PostGIS functions for location-based queries:

```swift
// Find posts within radius
let response = try await supabase
    .rpc("nearby_posts", params: [
        "lat": userLatitude,
        "long": userLongitude,
        "dist_meters": 5000
    ])
    .execute()
```

### Real-time Subscriptions

Subscribe to table changes for live updates:

```swift
// Subscribe to new messages in room
let channel = supabase.channel("room_participants:\(roomId)")
    .on(.postgresChanges(
        event: .insert,
        schema: "public",
        table: "room_participants",
        filter: "room_id=eq.\(roomId)"
    )) { payload in
        // Handle new message
    }
    .subscribe()
```

## Row Level Security (RLS)

All tables have RLS enabled. Common patterns:

**User owns record**:
```sql
-- Users can only update their own profile
USING (id = auth.uid())
```

**Public read, owner write**:
```sql
-- Anyone can view active posts
-- Only owner can update/delete
SELECT: is_active = true OR profile_id = auth.uid()
UPDATE/DELETE: profile_id = auth.uid()
```

**Room participants**:
```sql
-- Only room participants can view messages
USING (
    sharer = auth.uid() OR 
    requester = auth.uid()
)
```

## Data Validation

### Database Constraints

Respect database constraints in Swift code:

- **post_name**: Required, text field
- **post_description**: Optional, text field
- **rating**: 1-5 integer
- **review_text**: Optional, text field
- **message text**: Optional, text field
- **search_radius_km**: 1-100

### Swift Validation

Validate before sending to database:

```swift
struct FoodListingValidator {
    private let titleValidator = TitleValidator()
    private let locationValidator = LocationValidator()
    private let imagesValidator = ImagesValidator()
    
    func validate(_ listing: FoodListing) throws {
        // Post name: required, non-empty
        guard !listing.postName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            throw ValidationError.emptyTitle
        }
        
        // Location: valid coordinates if provided
        if let location = listing.location {
            try locationValidator.validate(location)
        }
        
        // Images: 1-3 images supported
        let imageCount = [listing.gifUrl, listing.gifUrl2, listing.gifUrl3].compactMap { $0 }.count
        guard imageCount >= 1 && imageCount <= 3 else {
            throw ValidationError.invalidImageCount
        }
    }
}

// Individual validators available in context/TypeGuards.swift:
// - TitleValidator: non-empty string
// - DescriptionValidator: optional text
// - MessageValidator: optional text
// - ReviewTextValidator: optional text
// - LocationValidator: valid lat/lon coordinates
// - ImagesValidator: 1-3 images, max 5MB each
// - EmailValidator: valid email format
// - PasswordValidator: min 8 chars, complexity requirements
// - RatingValidator: 1-5 integer
```

## Common Queries

### Fetch user profile with statistics
```swift
// Use profiles_with_stats view for unified access
let profile = try await supabase
    .from("profiles_with_stats")
    .select()
    .eq("id", value: userId)
    .single()
    .execute()

// Or join explicitly
let profile = try await supabase
    .from("profiles")
    .select("*, profile_stats(*)")
    .eq("id", value: userId)
    .single()
    .execute()
```

### Update user statistics
```swift
// Always update profile_stats table, not profiles
try await supabase
    .from("profile_stats")
    .update([
        "items_shared": newCount,
        "rating_average": newRating,
        "rating_count": reviewCount
    ])
    .eq("profile_id", value: userId)
    .execute()
```

### Fetch active posts
```swift
let posts = try await supabase
    .from("posts")
    .select()
    .eq("is_active", value: true)
    .eq("is_arranged", value: false)
    .order("created_at", ascending: false)
    .execute()
```

### Create room (conversation)
```swift
let room = try await supabase
    .from("rooms")
    .insert([
        "post_id": postId,
        "sharer": sharerId,
        "requester": requesterId
    ])
    .select()
    .single()
    .execute()
```

### Arrange post for pickup
```swift
try await supabase
    .from("posts")
    .update([
        "is_arranged": true,
        "post_arranged_to": userId,
        "post_arranged_at": Date()
    ])
    .eq("id", value: postId)
    .execute()
```

## Error Handling

Handle Supabase errors gracefully:

```swift
do {
    let response = try await supabase.from("posts").select().execute()
} catch let error as PostgrestError {
    // Handle database errors
    switch error.code {
    case "PGRST116": // No rows returned
        return []
    case "23505": // Unique violation
        throw AppError.duplicateEntry
    default:
        throw AppError.databaseError(error.message)
    }
}
```

## Performance Tips

- Use `.select()` to specify only needed columns
- Add indexes for frequently queried columns
- Use pagination for large result sets (`.range(from:to:)`)
- Cache frequently accessed data locally
- Use `.single()` when expecting one result
- Batch operations when possible

## Testing

Mock Supabase client for unit tests:

```swift
protocol SupabaseClientProtocol {
    func from(_ table: String) -> PostgrestQueryBuilder
}

class MockSupabaseClient: SupabaseClientProtocol {
    var mockData: [String: Any] = [:]
    
    func from(_ table: String) -> PostgrestQueryBuilder {
        // Return mock query builder
    }
}
```
