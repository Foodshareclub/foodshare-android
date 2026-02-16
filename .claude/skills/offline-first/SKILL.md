---
name: offline-first
description: Implement offline-first architecture for Foodshare iOS. Use for Core Data sync, optimistic updates, conflict resolution, and ensuring the app works without network connectivity.
---

<objective>
The app should work perfectly offline. Data syncs transparently when connectivity returns. Users never wait for network.
</objective>

<essential_principles>
## Offline-First Philosophy

**"Network is a nice-to-have, not a requirement."**

1. **Local first**: All reads/writes go to local storage immediately
2. **Background sync**: Network operations happen asynchronously
3. **Optimistic UI**: Show changes immediately, reconcile later
4. **Conflict resolution**: Handle concurrent edits gracefully

## Architecture Overview

```
┌─────────────────────────────────────┐
│            SwiftUI View             │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│           ViewModel                 │
│   (Reads from local, triggers sync) │
└────────────────┬────────────────────┘
                 │
┌────────────────▼────────────────────┐
│      OfflineFirstRepository         │
│   (Coordinates local + remote)      │
└───────┬───────────────────┬─────────┘
        │                   │
┌───────▼───────┐   ┌───────▼───────┐
│  Core Data    │   │   Supabase    │
│ (Local Store) │   │ (Remote API)  │
└───────────────┘   └───────────────┘
```

## Core Data Setup

```swift
// Core Data entity mirrors Supabase table
@objc(FoodListingEntity)
class FoodListingEntity: NSManagedObject {
    @NSManaged var id: UUID
    @NSManaged var title: String
    @NSManaged var syncStatus: String  // "synced", "pending", "conflict"
    @NSManaged var localUpdatedAt: Date
    @NSManaged var remoteUpdatedAt: Date?
}

// Sync status enum
enum SyncStatus: String {
    case synced = "synced"
    case pendingUpload = "pending_upload"
    case pendingDelete = "pending_delete"
    case conflict = "conflict"
}
```

## Offline-First Repository Pattern

```swift
final class OfflineFirstFoodRepository: FoodRepository {
    private let localStore: CoreDataStore
    private let remoteAPI: SupabaseAPI
    private let syncManager: SyncManager

    // Always read from local (instant)
    func fetchAll() async throws -> [FoodListing] {
        let entities = try localStore.fetchAll()
        return entities.map { $0.toDomain() }
    }

    // Write local first, sync later
    func create(_ listing: FoodListing) async throws -> FoodListing {
        // 1. Save locally with pending status
        let entity = try localStore.create(listing, syncStatus: .pendingUpload)

        // 2. Trigger background sync (non-blocking)
        Task.detached(priority: .utility) { [syncManager] in
            await syncManager.syncPendingChanges()
        }

        return entity.toDomain()
    }
}
```

## Sync Manager

```swift
actor SyncManager {
    private let localStore: CoreDataStore
    private let remoteAPI: SupabaseAPI
    private var isSyncing = false

    func syncPendingChanges() async {
        guard !isSyncing else { return }
        isSyncing = true
        defer { isSyncing = false }

        // Get all pending items
        let pending = try? await localStore.fetchPending()

        for item in pending ?? [] {
            do {
                switch item.syncStatus {
                case .pendingUpload:
                    try await uploadItem(item)
                case .pendingDelete:
                    try await deleteItem(item)
                case .conflict:
                    try await resolveConflict(item)
                default:
                    break
                }
            } catch {
                // Mark for retry, don't crash
                await localStore.markForRetry(item)
            }
        }
    }
}
```

## Conflict Resolution Strategies

```swift
enum ConflictResolution {
    case serverWins    // Remote version takes precedence
    case clientWins    // Local version takes precedence
    case merge         // Combine changes (complex)
    case askUser       // Present choice to user
}

// Simple: Last-write-wins based on timestamp
func resolveConflict(_ local: FoodListingEntity, _ remote: FoodListingDTO) -> FoodListing {
    if local.localUpdatedAt > (remote.updatedAt ?? .distantPast) {
        // Local is newer, push to server
        return local.toDomain()
    } else {
        // Remote is newer, accept server version
        return remote.toDomain()
    }
}
```

## Network Reachability

```swift
@Observable
final class NetworkMonitor {
    var isConnected = true

    private let monitor = NWPathMonitor()

    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            Task { @MainActor in
                self?.isConnected = path.status == .satisfied
            }
        }
        monitor.start(queue: .global(qos: .utility))
    }
}

// Trigger sync when connection restored
.onChange(of: networkMonitor.isConnected) { _, isConnected in
    if isConnected {
        Task { await syncManager.syncPendingChanges() }
    }
}
```
</essential_principles>

<intake>
What offline functionality do you need help with?

1. **Setup Core Data** - Initialize local persistence
2. **Implement sync** - Background sync with Supabase
3. **Handle conflicts** - Concurrent edit resolution
4. **Optimistic UI** - Instant local updates
5. **Test offline** - Verify offline behavior
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "core data", "setup", "local" | workflows/setup-core-data.md |
| 2, "sync", "background", "supabase" | workflows/implement-sync.md |
| 3, "conflict", "resolution", "merge" | workflows/handle-conflicts.md |
| 4, "optimistic", "instant", "ui" | workflows/optimistic-updates.md |
| 5, "test", "offline", "verify" | workflows/test-offline.md |
</routing>

<quick_reference>
## Quick Patterns

### Check Sync Status
```swift
// Show sync indicator
if listing.syncStatus != .synced {
    Image(systemName: "arrow.triangle.2.circlepath")
        .foregroundColor(.orange)
}
```

### Handle Network Errors
```swift
do {
    try await remoteAPI.create(listing)
    await localStore.markSynced(listing)
} catch {
    // Keep local, retry later
    await localStore.markPendingUpload(listing)
    NotificationCenter.default.post(name: .syncFailed, object: nil)
}
```

### Offline Banner
```swift
if !networkMonitor.isConnected {
    GlassOfflineBanner()
        .transition(.move(edge: .top))
}
```

### Force Sync
```swift
Button("Sync Now") {
    Task {
        await syncManager.forceSyncAll()
    }
}
.disabled(!networkMonitor.isConnected)
```
</quick_reference>

<success_criteria>
Offline-first is complete when:
- [ ] App launches and displays data without network
- [ ] CRUD operations work offline
- [ ] Changes sync when connectivity returns
- [ ] Conflicts are resolved (not lost)
- [ ] User sees sync status indicators
- [ ] No data loss in any scenario
- [ ] Background sync doesn't block UI
</success_criteria>
