---
name: foodshare-testing
description: Implement comprehensive Swift Testing for Foodshare iOS. Use for writing tests, debugging failures, setting up mocks, or improving coverage. Targets 70%+ coverage with confidence-focused testing.
---

<objective>
Write tests that provide confidence, not just coverage. Every test should answer: "If this breaks at 2 AM, how quickly can I find the bug?"
</objective>

<essential_principles>
## Testing Philosophy

**"Tests aren't bureaucracy. They're life insurance."**

### The Testing Pyramid
```
       /\
      /UI\        10% - Critical user flows only
     /    \
    /      \
   / Integ  \     20% - Layer interactions
  /          \
 /            \
/     Unit     \  70% - Logic and state
/________________\
```

### What to Test (Always)
- **ViewModels**: State management, loading, errors
- **Use Cases**: Business logic orchestration
- **Repositories**: Data transformation, error mapping

### What NOT to Test (Never)
- **SwiftUI Views**: Too brittle, low value
- **Simple models**: No logic = no test
- **Infrastructure**: Don't test Supabase itself
- **Constants**: No behavior to verify

### Swift Testing Framework (iOS 17+)
```swift
import Testing

@Test("Loads listings successfully")
func loadListings() async throws {
    // Arrange, Act, Assert
    #expect(viewModel.listings.count == 5)
}

@Test(arguments: [0, -1, 101])
func invalidRadius(_ radius: Int) {
    #expect(throws: ValidationError.self) {
        try validate(radius)
    }
}
```
</essential_principles>

<intake>
What testing task do you need help with?

1. **Write tests** - Create tests for existing code
2. **Debug failure** - Fix failing tests
3. **Create mocks** - Build mock objects for testing
4. **Improve coverage** - Find untested code paths
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "write", "create", "new tests" | workflows/write-tests.md |
| 2, "debug", "fix", "failing" | workflows/debug-tests.md |
| 3, "mock", "fake", "stub" | workflows/create-mocks.md |
| 4, "coverage", "improve", "untested" | workflows/improve-coverage.md |
</routing>

<quick_reference>
## Test File Structure

```
tests/FoodshareTests/
├── Unit/
│   ├── {Feature}/
│   │   ├── {Feature}ViewModelTests.swift
│   │   └── {UseCase}Tests.swift
│   └── Core/
│       └── {Service}Tests.swift
├── Integration/
│   └── {Feature}IntegrationTests.swift
└── Mocks/
    ├── Mock{Repository}.swift
    └── Mock{Service}.swift
```

## ViewModel Test Template

```swift
import Testing
@testable import Foodshare

@MainActor
struct FeedViewModelTests {
    let mockRepository = MockFoodRepository()
    let mockLocation = MockLocationService()

    @Test("Loads listings successfully")
    func loadSuccess() async {
        // Arrange
        mockRepository.items = [.fixture(), .fixture()]
        let viewModel = FeedViewModel(
            repository: mockRepository,
            locationService: mockLocation
        )

        // Act
        await viewModel.load()

        // Assert
        #expect(viewModel.listings.count == 2)
        #expect(viewModel.isLoading == false)
        #expect(viewModel.error == nil)
    }

    @Test("Shows error on failure")
    func loadFailure() async {
        // Arrange
        mockRepository.shouldFail = true
        let viewModel = FeedViewModel(
            repository: mockRepository,
            locationService: mockLocation
        )

        // Act
        await viewModel.load()

        // Assert
        #expect(viewModel.listings.isEmpty)
        #expect(viewModel.error != nil)
    }

    @Test("Prevents concurrent loads")
    func preventsConcurrentLoads() async {
        // Arrange
        mockRepository.delay = 0.1
        let viewModel = FeedViewModel(
            repository: mockRepository,
            locationService: mockLocation
        )

        // Act
        async let load1: () = viewModel.load()
        async let load2: () = viewModel.load()
        await load1
        await load2

        // Assert
        #expect(mockRepository.fetchCount == 1)
    }
}
```

## Mock Template

```swift
final class MockFoodRepository: FoodRepository, @unchecked Sendable {
    // Tracking
    var fetchCount = 0
    var lastQuery: String?

    // Stubbing
    var items: [FoodListing] = []
    var shouldFail = false
    var delay: TimeInterval = 0

    func fetchAll() async throws -> [FoodListing] {
        fetchCount += 1
        if delay > 0 {
            try await Task.sleep(for: .seconds(delay))
        }
        if shouldFail {
            throw TestError.mockFailure
        }
        return items
    }
}
```

## Fixture Pattern

```swift
extension FoodListing {
    static func fixture(
        id: UUID = UUID(),
        title: String = "Test Listing",
        category: ListingCategory = .produce
    ) -> FoodListing {
        FoodListing(
            id: id,
            title: title,
            category: category,
            createdAt: Date()
        )
    }
}
```
</quick_reference>

<success_criteria>
Tests are good when:
- [ ] Each test has clear Arrange/Act/Assert sections
- [ ] Tests use descriptive names explaining what's tested
- [ ] Mocks track calls AND allow stubbing responses
- [ ] Async code is properly awaited
- [ ] No real network/database calls in unit tests
- [ ] Fixtures make test data creation easy
- [ ] Error cases are tested, not just happy paths
</success_criteria>
