---
name: swift-concurrency
description: Implement Swift 6.2 concurrency patterns for Foodshare iOS. Use for async/await, actors, Sendable compliance, TaskGroups, and data race prevention. Ensures thread safety and performance.
---

<objective>
Write concurrent code that is correct by construction. No data races, no deadlocks, full Sendable compliance.
</objective>

<essential_principles>
## Swift 6.2 Concurrency Model

**Core Concepts:**
- **async/await** - Structured asynchronous code
- **Actor** - Thread-safe mutable state isolation
- **Sendable** - Types safe to pass across concurrency boundaries
- **@MainActor** - Execute on main thread
- **TaskGroup** - Concurrent parallel work

## Sendable Compliance (Non-Negotiable)

Swift 6 enforces Sendable at compile time. All types crossing concurrency boundaries must be Sendable.

```swift
// ✅ Value types are Sendable by default
struct FoodListing: Sendable {
    let id: UUID
    let title: String
}

// ✅ Actors are Sendable
actor ImageCache {
    private var cache: [URL: UIImage] = [:]
}

// ✅ Classes with @unchecked Sendable (when you KNOW it's safe)
final class SupabaseRepository: @unchecked Sendable {
    private let client: SupabaseClient  // Client handles its own thread safety
}

// ❌ WRONG - Mutable class not Sendable
class BadCache {
    var items: [String] = []  // Data race possible
}
```

## Actor Isolation Rules

```swift
// All ViewModel state must be @MainActor
@MainActor
@Observable
final class FeedViewModel {
    var items: [FoodListing] = []  // Safe - main actor isolated
    var isLoading = false

    func load() async {
        // This runs on main actor
        isLoading = true
        items = await repository.fetch()  // Awaits, may suspend
        isLoading = false
    }
}

// Actor for shared mutable state
actor DataCache {
    private var data: [String: Data] = [:]

    func get(_ key: String) -> Data? {
        data[key]  // Actor-isolated, no await needed inside
    }

    func set(_ key: String, value: Data) {
        data[key] = value
    }
}

// Calling actor from outside requires await
let cache = DataCache()
let value = await cache.get("key")  // Must await
```

## Structured Concurrency Patterns

```swift
// TaskGroup for parallel operations
func fetchAllCategories() async throws -> [Category] {
    try await withThrowingTaskGroup(of: [FoodListing].self) { group in
        for category in Category.allCases {
            group.addTask {
                try await repository.fetch(category: category)
            }
        }

        var allListings: [FoodListing] = []
        for try await listings in group {
            allListings.append(contentsOf: listings)
        }
        return allListings
    }
}

// AsyncStream for continuous events
func locationUpdates() -> AsyncStream<Location> {
    AsyncStream { continuation in
        let observer = locationManager.observe { location in
            continuation.yield(location)
        }

        continuation.onTermination = { _ in
            observer.cancel()
        }
    }
}
```

## Red Flags (Concurrency Anti-Patterns)

```swift
// ❌ Capturing mutable state in Task
var count = 0
Task {
    count += 1  // Data race!
}

// ❌ DispatchQueue.main.async in async context
func load() async {
    DispatchQueue.main.async {  // WRONG - breaks structured concurrency
        self.items = data
    }
}

// ✅ Use @MainActor instead
@MainActor
func load() async {
    items = await fetch()  // Already on main actor
}

// ❌ Force-unwrapping Task result
let task = Task { await fetch() }
let result = task.value!  // Blocks, defeats async purpose

// ✅ Await properly
let result = await task.value
```
</essential_principles>

<intake>
What concurrency task do you need help with?

1. **Make Sendable** - Fix Sendable compliance errors
2. **Create actor** - Thread-safe state container
3. **Parallel work** - TaskGroup implementation
4. **Async stream** - Reactive async sequences
5. **Fix data race** - Debug concurrency issues
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "sendable", "compliance", "error" | workflows/make-sendable.md |
| 2, "actor", "isolated", "thread-safe" | workflows/create-actor.md |
| 3, "parallel", "taskgroup", "concurrent" | workflows/parallel-work.md |
| 4, "stream", "asyncstream", "reactive" | workflows/async-stream.md |
| 5, "data race", "debug", "crash" | workflows/fix-data-race.md |
</routing>

<quick_reference>
## Common Patterns

### Safe Repository Pattern
```swift
protocol FoodRepository: Sendable {
    func fetch() async throws -> [FoodListing]
}

final class SupabaseFoodRepository: FoodRepository, @unchecked Sendable {
    private let client: SupabaseClient

    func fetch() async throws -> [FoodListing] {
        try await client.from("food_items").select().execute().value
    }
}
```

### Main Actor ViewModel
```swift
@MainActor
@Observable
final class ListViewModel {
    var items: [Item] = []
    var isLoading = false
    var error: Error?

    private let repository: ItemRepository

    init(repository: ItemRepository) {
        self.repository = repository
    }

    func load() async {
        guard !isLoading else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            items = try await repository.fetch()
        } catch {
            self.error = error
        }
    }
}
```

### Cancellation-Safe Task
```swift
func search(query: String) async {
    searchTask?.cancel()
    searchTask = Task {
        try? await Task.sleep(for: .milliseconds(300))  // Debounce

        guard !Task.isCancelled else { return }

        let results = try? await repository.search(query)

        guard !Task.isCancelled else { return }

        await MainActor.run {
            self.results = results ?? []
        }
    }
}
```

### AsyncSequence Transformation
```swift
// Filter async sequence
for await location in locationStream where location.accuracy < 10 {
    updateMap(location)
}

// Map async sequence
let names = listings.map(\.title)
for await name in names {
    print(name)
}
```
</quick_reference>

<success_criteria>
Concurrency is correct when:
- [ ] No Sendable warnings in build
- [ ] No data race crashes in runtime
- [ ] All ViewModels are @MainActor
- [ ] All shared mutable state is in actors
- [ ] Tasks are properly cancelled when no longer needed
- [ ] No DispatchQueue usage in async code
- [ ] No force-awaiting (blocking) task results
</success_criteria>
