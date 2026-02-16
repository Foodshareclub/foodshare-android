# Review Architecture Workflow

<required_reading>
- references/layer-rules.md
- references/red-flags.md
</required_reading>

<process>
## Step 1: Identify Scope

Ask: "What code should I review?"
- Single file
- Feature module
- PR diff
- Entire codebase

## Step 2: Check Layer Violations

For each file, verify:

**Domain Layer Files:**
```swift
// ❌ VIOLATION: Infrastructure import in domain
import Supabase  // Domain must not know about this
import UIKit     // Domain must not know about this

// ❌ VIOLATION: Codable in domain model
struct FoodListing: Codable { }  // DTOs are Codable, models are not

// ✅ CORRECT: Pure Swift
struct FoodListing {
    let id: UUID
    let title: String
}
```

**Data Layer Files:**
```swift
// ✅ CORRECT: Implements domain protocol
final class SupabaseFoodRepository: FoodRepository {
    private let client: SupabaseClient
    // Implementation details...
}
```

**Presentation Layer Files:**
```swift
// ❌ VIOLATION: Direct service call in ViewModel
class FeedViewModel {
    func load() async {
        let data = try await supabase.from("foods").select()  // WRONG
    }
}

// ✅ CORRECT: Uses injected use case
class FeedViewModel {
    private let fetchUseCase: FetchFoodsUseCase

    func load() async {
        items = try await fetchUseCase.execute()  // RIGHT
    }
}

// ❌ VIOLATION: Async logic in View
struct FeedView: View {
    var body: some View {
        Button("Load") {
            Task {
                let data = try await repository.fetch()  // WRONG
            }
        }
    }
}

// ✅ CORRECT: Delegates to ViewModel
struct FeedView: View {
    @State private var viewModel: FeedViewModel

    var body: some View {
        Button("Load") {
            Task { await viewModel.load() }  // RIGHT
        }
    }
}
```

## Step 3: Check Feature Boundaries

```swift
// ❌ VIOLATION: Cross-feature import
import Foodshare.Features.Feed  // From Profile feature - WRONG

// ✅ CORRECT: Import only Core
import Foodshare.Core.Design
import Foodshare.Core.Networking
```

## Step 4: Check Dependency Injection

```swift
// ❌ VIOLATION: Creating dependencies internally
class FeedViewModel {
    private let repository = SupabaseFoodRepository()  // WRONG
}

// ✅ CORRECT: Injected via init
class FeedViewModel {
    private let repository: FoodRepository  // Protocol type

    init(repository: FoodRepository) {  // RIGHT
        self.repository = repository
    }
}
```

## Step 5: Report Findings

Format findings as:

```
## Architecture Review: {Scope}

### ✅ Correct Patterns
- [List good patterns found]

### ❌ Violations Found
1. **{File}:{Line}** - {Description}
   - Current: `{code}`
   - Should be: `{fix}`

### 📋 Recommendations
- [List suggested improvements]
```
</process>

<success_criteria>
Review is complete when:
- All files in scope have been checked
- All violations are documented with line numbers
- Each violation has a suggested fix
- Severity is indicated (blocking vs. advisory)
</success_criteria>
