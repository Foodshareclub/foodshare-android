---
name: supabase-workflow
description: Manage Supabase database, RLS policies, Edge Functions, and backend operations for Foodshare. Use for migrations, queries, security, and performance optimization.
---

<objective>
Backend infrastructure should be invisible, reliable, and fast. Users don't care about Supabase—they care that food appears instantly.
</objective>

<essential_principles>
## Supabase Configuration

- **Database**: PostgreSQL 15 with PostGIS extension
- **Auth**: Email/password, magic link, social providers
- **Storage**: food-images bucket for listing photos
- **Realtime**: Enabled for messaging and notifications
- **Edge Functions**: Deno/TypeScript serverless functions

## Security Rules (Non-Negotiable)

1. **ALWAYS enable RLS on every table**
```sql
ALTER TABLE table_name ENABLE ROW LEVEL SECURITY;
```

2. **NEVER expose service_role key in iOS**
```swift
// ❌ WRONG - Service role in client
let key = "service_role_key"

// ✅ RIGHT - Anon key only
let key = Environment.supabasePublishableKey
```

3. **ALWAYS validate in Edge Functions**
```typescript
const { userId, data } = await req.json()
if (!userId || !data) throw new Error("Missing fields")
```

## Migration Best Practices

1. **Use transactions**
```sql
BEGIN;
-- changes
COMMIT;
```

2. **Create proper indexes**
```sql
CREATE INDEX idx_food_items_location ON food_items USING GIST(location);
CREATE INDEX idx_food_items_user_created ON food_items(user_id, created_at DESC);
```

3. **Never modify existing migrations** - Create new ones instead

## RLS Policy Patterns

```sql
-- Anyone can read available items
CREATE POLICY "Public read available"
    ON food_items FOR SELECT
    USING (status = 'available');

-- Users manage their own items
CREATE POLICY "Users manage own"
    ON food_items FOR ALL
    USING (auth.uid() = user_id);
```
</essential_principles>

<intake>
What Supabase task do you need help with?

1. **Create migration** - New table, column, or index
2. **RLS policies** - Security rules for tables
3. **Edge Function** - Serverless backend logic
4. **Query optimization** - Fix slow queries
5. **Storage** - File upload/download configuration
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "migration", "table", "column" | workflows/create-migration.md |
| 2, "rls", "policy", "security" | workflows/create-rls.md |
| 3, "edge", "function", "serverless" | workflows/create-edge-function.md |
| 4, "query", "slow", "performance" | workflows/optimize-query.md |
| 5, "storage", "upload", "images" | workflows/configure-storage.md |
</routing>

<quick_reference>
## Common Commands

```bash
# Local development
npx supabase start
npx supabase stop

# Migrations
npx supabase migration new add_feature_table
npx supabase db reset  # Apply all migrations
npx supabase db push   # Push to remote

# Edge Functions
npx supabase functions new function-name
npx supabase functions serve function-name
npx supabase functions deploy function-name

# Types
npx supabase gen types typescript --local > types/supabase.ts
```

## Database Function for Geo Search

```sql
CREATE OR REPLACE FUNCTION search_nearby(
    user_lat DOUBLE PRECISION,
    user_lng DOUBLE PRECISION,
    radius_m INTEGER DEFAULT 5000
)
RETURNS SETOF food_items
LANGUAGE SQL STABLE
AS $$
    SELECT *
    FROM food_items
    WHERE status = 'available'
    AND ST_DWithin(
        location,
        ST_SetSRID(ST_MakePoint(user_lng, user_lat), 4326)::geography,
        radius_m
    )
    ORDER BY ST_Distance(
        location,
        ST_SetSRID(ST_MakePoint(user_lng, user_lat), 4326)::geography
    )
    LIMIT 50;
$$;
```

## iOS Client Usage

```swift
// Query with RPC
let items: [FoodListingDTO] = try await supabase
    .rpc("search_nearby", params: [
        "user_lat": location.latitude,
        "user_lng": location.longitude,
        "radius_m": 5000
    ])
    .execute()
    .value

// Realtime subscription
let channel = supabase.realtime.channel("messages")
await channel
    .on(.postgresChanges(
        InsertAction.self,
        schema: "public",
        table: "messages",
        filter: "recipient_id=eq.\(userId)"
    )) { change in
        handleNewMessage(change.new)
    }
    .subscribe()
```

## Storage Upload

```swift
func uploadImage(_ image: UIImage) async throws -> URL {
    guard let data = image.jpegData(compressionQuality: 0.8) else {
        throw AppError.invalidImage
    }

    let path = "listings/\(UUID().uuidString).jpg"

    try await supabase.storage
        .from("food-images")
        .upload(path: path, file: data)

    return try supabase.storage
        .from("food-images")
        .getPublicURL(path: path)
}
```
</quick_reference>

<success_criteria>
Supabase implementation is correct when:
- [ ] All tables have RLS enabled
- [ ] All foreign keys are indexed
- [ ] Location queries use PostGIS spatial indexes
- [ ] Edge Functions validate all input
- [ ] Service role key never appears in iOS code
- [ ] Migrations are idempotent (can run multiple times)
- [ ] Storage policies restrict uploads to authenticated users
</success_criteria>
