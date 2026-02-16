# Make Sendable Workflow

<required_reading>
- references/sendable-rules.md
</required_reading>

<process>
## Step 1: Identify the Error

Common Sendable warnings:
- "Type 'X' does not conform to 'Sendable'"
- "Capture of 'self' with non-sendable type"
- "Passing closure of non-sendable type"

## Step 2: Analyze the Type

**Is it a value type (struct/enum)?**
```swift
// Struct with only Sendable properties → already Sendable
struct FoodListing: Sendable {
    let id: UUID           // ✅ UUID is Sendable
    let title: String      // ✅ String is Sendable
    let date: Date         // ✅ Date is Sendable
}
```

**Is it a reference type (class)?**
```swift
// Options for classes:
// 1. Make it an actor
actor ImageCache { }

// 2. Use @unchecked Sendable (when you KNOW it's thread-safe)
final class SupabaseRepository: @unchecked Sendable {
    private let client: SupabaseClient  // Thread-safe internally
}

// 3. Make it @MainActor isolated
@MainActor
final class ViewModel { }
```

## Step 3: Fix Common Patterns

### Pattern: Repository Protocol
```swift
// Protocol must be Sendable
protocol FoodRepository: Sendable {
    func fetch() async throws -> [FoodListing]
}

// Implementation uses @unchecked Sendable
final class SupabaseFoodRepository: FoodRepository, @unchecked Sendable {
    private let client: SupabaseClient

    init(client: SupabaseClient) {
        self.client = client
    }
}
```

### Pattern: Closure Capture
```swift
// ❌ Error: Capture of 'self' with non-sendable type
class BadViewModel {
    var data: [String] = []

    func load() {
        Task {
            data = await fetch()  // Non-sendable capture
        }
    }
}

// ✅ Fix: Make ViewModel @MainActor
@MainActor
@Observable
final class GoodViewModel {
    var data: [String] = []

    func load() async {
        data = await fetch()  // Safe - main actor isolated
    }
}
```

### Pattern: Passing Data Between Tasks
```swift
// ❌ Error: Non-sendable type in Task
let mutableArray = NSMutableArray()
Task {
    mutableArray.add("item")  // Not Sendable
}

// ✅ Fix: Use Sendable type
var items: [String] = []
Task { @MainActor in
    items.append("item")
}
```

## Step 4: Verify Fix

1. Build the project
2. Check for remaining Sendable warnings
3. Run tests to ensure behavior unchanged
</process>

<success_criteria>
Sendable compliance achieved when:
- [ ] No Sendable warnings in build
- [ ] All repository protocols are Sendable
- [ ] All ViewModels are @MainActor
- [ ] All actors properly isolate mutable state
- [ ] Tests still pass
</success_criteria>
