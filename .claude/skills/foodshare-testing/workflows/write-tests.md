# Write Tests Workflow

<required_reading>
- references/testing-patterns.md
- templates/test-templates.md
</required_reading>

<process>
## Step 1: Identify Test Subject

Ask: "What code needs tests?"
- ViewModel
- Use Case
- Repository
- Service
- Utility function

## Step 2: Analyze Dependencies

For each dependency:
1. Is there already a mock? Check `tests/Mocks/`
2. If not, create a mock (see workflows/create-mocks.md)

## Step 3: Identify Test Cases

For ViewModels, always test:
- [ ] Initial state
- [ ] Successful load
- [ ] Failed load (error handling)
- [ ] Loading state management
- [ ] Concurrent operation prevention
- [ ] State reset/clear

For Use Cases, always test:
- [ ] Happy path with valid input
- [ ] Edge cases (empty, nil, boundaries)
- [ ] Error propagation
- [ ] Business rule enforcement

For Repositories, always test:
- [ ] Data transformation (DTO → Domain)
- [ ] Error mapping
- [ ] Query construction

## Step 4: Write Tests Using Swift Testing

```swift
import Testing
@testable import Foodshare

@MainActor
struct {Subject}Tests {
    // Setup - create mocks
    let mockDependency = MockDependency()

    @Test("Description of what is being tested")
    func testName() async throws {
        // Arrange - set up conditions
        mockDependency.stubbedValue = expectedValue

        // Act - perform the action
        let result = await subject.performAction()

        // Assert - verify expectations
        #expect(result == expected)
        #expect(mockDependency.actionWasCalled)
    }
}
```

## Step 5: Test Edge Cases

```swift
@Test(arguments: ["", " ", "   "])
func rejectsEmptyTitle(_ title: String) {
    #expect(throws: ValidationError.emptyTitle) {
        try Listing.validate(title: title)
    }
}

@Test("Handles nil response gracefully")
func handlesNilResponse() async {
    mockRepository.returnNil = true

    await viewModel.load()

    #expect(viewModel.items.isEmpty)
    #expect(viewModel.error == nil) // Nil is valid, not error
}
```

## Step 6: Verify Test Quality

Run tests:
```bash
xcodebuild test -scheme Foodshare -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max'
```

Check that:
- All tests pass
- Tests fail when code is broken
- Tests don't depend on order
- Tests run fast (<1s per test)
</process>

<anti_patterns>
## Don't Do This

**No assertions:**
```swift
// ❌ BAD - doesn't verify anything
@Test func loadListings() async {
    await viewModel.load()
}
```

**Testing implementation details:**
```swift
// ❌ BAD - tests private state
#expect(viewModel._internalCounter == 5)
```

**Flaky timing:**
```swift
// ❌ BAD - depends on real time
try await Task.sleep(for: .seconds(5))
```

**Testing infrastructure:**
```swift
// ❌ BAD - tests Supabase, not our code
@Test func supabaseConnects() async {
    let client = SupabaseClient(...)
    // This tests Supabase, not Foodshare
}
```
</anti_patterns>

<success_criteria>
Test suite is complete when:
- All ViewModels have success/failure/loading tests
- All Use Cases have happy path + edge case tests
- No real infrastructure in unit tests
- Tests run in <30 seconds total
- Code coverage ≥70% for tested code
</success_criteria>
