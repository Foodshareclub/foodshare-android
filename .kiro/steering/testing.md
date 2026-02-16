---
inclusion: always
---

# Testing Guidelines

## Testing Strategy

Follow the **70/20/10 pyramid**:
- 70% Unit Tests (fast, isolated)
- 20% Integration Tests (layer interactions)
- 10% UI Tests (critical flows only)

Target: 80%+ coverage for business logic, 70%+ overall

## Unit Testing

### Test Structure

Use **Arrange-Act-Assert** pattern:

```swift
import Testing
@testable import Foodshare

@Test("Food listing validation succeeds with valid data")
func testValidListingValidation() async throws {
    // Arrange
    let listing = FoodListing.fixture(
        title: "Fresh Apples",
        expiryDate: Date().addingTimeInterval(86400)
    )
    let validator = FoodListingValidator()
    
    // Act & Assert
    #expect(throws: Never.self) {
        try validator.validate(listing)
    }
}

@Test("Food listing validation fails with expired date")
func testExpiredListingValidation() async throws {
    // Arrange
    let listing = FoodListing.fixture(
        expiryDate: Date().addingTimeInterval(-86400)
    )
    let validator = FoodListingValidator()
    
    // Act & Assert
    #expect(throws: ValidationError.expiredDate) {
        try validator.validate(listing)
    }
}
```

### ViewModel Testing

```swift
import XCTest
@testable import Foodshare

@MainActor
final class FoodListingViewModelTests: XCTestCase {
    var viewModel: FoodListingViewModel!
    var mockRepository: MockFoodListingRepository!
    
    override func setUp() async throws {
        mockRepository = MockFoodListingRepository()
        let useCase = FetchListingsUseCase(repository: mockRepository)
        viewModel = FoodListingViewModel(fetchListingsUseCase: useCase)
    }
    
    override func tearDown() async throws {
        viewModel = nil
        mockRepository = nil
    }
    
    func testLoadListingsSuccess() async throws {
        // Arrange
        let expected = [
            FoodListing.fixture(title: "Apples"),
            FoodListing.fixture(title: "Bread")
        ]
        mockRepository.mockListings = expected
        
        // Act
        await viewModel.loadListings()
        
        // Assert
        XCTAssertEqual(viewModel.listings.count, 2)
        XCTAssertEqual(viewModel.listings[0].title, "Apples")
        XCTAssertFalse(viewModel.isLoading)
        XCTAssertNil(viewModel.error)
    }
    
    func testLoadListingsFailure() async throws {
        // Arrange
        mockRepository.shouldFail = true
        
        // Act
        await viewModel.loadListings()
        
        // Assert
        XCTAssertTrue(viewModel.listings.isEmpty)
        XCTAssertNotNil(viewModel.error)
        XCTAssertFalse(viewModel.isLoading)
    }
    
    func testLoadingStateManagement() async throws {
        // Arrange
        mockRepository.mockListings = [FoodListing.fixture()]
        
        // Act
        let loadingTask = Task {
            await viewModel.loadListings()
        }
        
        // Assert - loading state
        XCTAssertTrue(viewModel.isLoading)
        
        await loadingTask.value
        
        // Assert - completed state
        XCTAssertFalse(viewModel.isLoading)
    }
}
```

### Use Case Testing

```swift
@Test("Fetch listings filters expired items")
func testFetchListingsFiltersExpired() async throws {
    // Arrange
    let mockRepo = MockFoodListingRepository()
    mockRepo.mockListings = [
        FoodListing.fixture(
            title: "Fresh",
            expiryDate: Date().addingTimeInterval(86400),
            status: .available
        ),
        FoodListing.fixture(
            title: "Expired",
            expiryDate: Date().addingTimeInterval(-86400),
            status: .available
        )
    ]
    let useCase = FetchListingsUseCase(repository: mockRepo)
    
    // Act
    let result = try await useCase.execute(
        near: Location.fixture(),
        radius: 5.0
    )
    
    // Assert
    #expect(result.count == 1)
    #expect(result[0].title == "Fresh")
}
```

## Test Fixtures

Create reusable test data:

```swift
extension FoodListing {
    static func fixture(
        id: Int = 1,
        userId: UUID = UUID(),
        title: String = "Fresh Apples",
        description: String? = "Delicious organic apples",
        quantity: String = "5 apples",
        imageUrls: [String] = ["https://example.com/apple.jpg"],
        expiryDate: Date = Date().addingTimeInterval(86400),
        pickupLatitude: Double = 51.5074,
        pickupLongitude: Double = -0.1278,
        status: FoodItemStatus = .available,
        viewCount: Int = 0,
        createdAt: Date = Date(),
        updatedAt: Date = Date()
    ) -> FoodListing {
        FoodListing(
            id: id,
            userId: userId,
            categoryId: 1,
            title: title,
            description: description,
            quantity: quantity,
            imageUrls: imageUrls,
            primaryImageUrl: imageUrls.first,
            expiryDate: expiryDate,
            pickupLatitude: pickupLatitude,
            pickupLongitude: pickupLongitude,
            pickupAddress: "123 Main St",
            status: status,
            viewCount: viewCount,
            createdAt: createdAt,
            updatedAt: updatedAt
        )
    }
}

extension Location {
    static func fixture(
        latitude: Double = 51.5074,
        longitude: Double = -0.1278
    ) -> Location {
        Location(latitude: latitude, longitude: longitude)
    }
}
```

## Mock Objects

### Repository Mocks

```swift
final class MockFoodListingRepository: FoodListingRepository {
    var mockListings: [FoodListing] = []
    var shouldFail = false
    var fetchCallCount = 0
    
    func fetchListings(near location: Location, radius: Double) async throws -> [FoodListing] {
        fetchCallCount += 1
        
        if shouldFail {
            throw AppError.networkError("Mock error")
        }
        
        return mockListings
    }
    
    func createListing(_ listing: FoodListing) async throws -> FoodListing {
        if shouldFail {
            throw AppError.networkError("Mock error")
        }
        
        var created = listing
        created.id = Int.random(in: 1...1000)
        mockListings.append(created)
        return created
    }
}
```

### Supabase Client Mock

```swift
protocol SupabaseClientProtocol {
    func from(_ table: String) -> PostgrestQueryBuilder
}

final class MockSupabaseClient: SupabaseClientProtocol {
    var mockData: [String: [[String: Any]]] = [:]
    var shouldFail = false
    
    func from(_ table: String) -> PostgrestQueryBuilder {
        // Return mock query builder with test data
        MockPostgrestQueryBuilder(
            data: mockData[table] ?? [],
            shouldFail: shouldFail
        )
    }
}
```

## Integration Tests

Test layer interactions:

```swift
final class SupabaseFoodListingRepositoryTests: XCTestCase {
    var repository: SupabaseFoodListingRepository!
    var supabase: SupabaseClient!
    
    override func setUp() async throws {
        // Use test Supabase instance
        supabase = SupabaseClient(
            supabaseURL: URL(string: "https://api.foodshare.club")!,
            supabaseKey: "test-key"
        )
        repository = SupabaseFoodListingRepository(supabase: supabase)
    }
    
    func testFetchListingsIntegration() async throws {
        // This test hits real Supabase test environment
        let location = Location(latitude: 51.5074, longitude: -0.1278)
        let listings = try await repository.fetchListings(
            near: location,
            radius: 5.0
        )
        
        XCTAssertNotNil(listings)
        // Verify response structure
        if let first = listings.first {
            XCTAssertNotNil(first.id)
            XCTAssertNotNil(first.title)
        }
    }
}
```

## UI Tests

Test critical user flows:

```swift
final class CreateListingUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUp() {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["--uitesting", "--mock-backend"]
        app.launch()
    }
    
    func testCreateListingFlow() throws {
        // Navigate to create listing
        app.tabBars.buttons["Create"].tap()
        
        // Fill form
        let titleField = app.textFields["Title"]
        XCTAssertTrue(titleField.waitForExistence(timeout: 2))
        titleField.tap()
        titleField.typeText("Fresh Apples")
        
        let descriptionField = app.textViews["Description"]
        descriptionField.tap()
        descriptionField.typeText("5 organic apples from my garden")
        
        // Select category
        app.buttons["Category"].tap()
        app.buttons["Produce"].tap()
        
        // Set expiry date
        app.buttons["Expiry Date"].tap()
        // ... date picker interaction
        
        // Add photo
        app.buttons["Add Photo"].tap()
        // ... photo picker interaction
        
        // Submit
        app.buttons["Create Listing"].tap()
        
        // Verify success
        XCTAssertTrue(
            app.staticTexts["Fresh Apples"]
                .waitForExistence(timeout: 5)
        )
    }
}
```

## Test Best Practices

### Do's
- ✅ Test one thing per test
- ✅ Use descriptive test names
- ✅ Follow Arrange-Act-Assert pattern
- ✅ Use fixtures for test data
- ✅ Mock external dependencies
- ✅ Test edge cases and error paths
- ✅ Keep tests fast (unit tests < 100ms)
- ✅ Make tests independent (no shared state)

### Don'ts
- ❌ Test implementation details
- ❌ Write tests that depend on other tests
- ❌ Use real network calls in unit tests
- ❌ Test framework code (SwiftUI, Supabase)
- ❌ Ignore flaky tests
- ❌ Skip error case testing
- ❌ Hard-code test data inline

## Running Tests

### Command Line
```bash
# Run all tests
xcodebuild test -scheme Foodshare -destination 'platform=iOS Simulator,name=iPhone 17 Pro Max'

# Run specific test
xcodebuild test -scheme Foodshare -only-testing:FoodshareTests/FoodListingViewModelTests/testLoadListingsSuccess

# Run with coverage
xcodebuild test -scheme Foodshare -enableCodeCoverage YES
```

### Xcode
- Cmd+U: Run all tests
- Cmd+Ctrl+U: Run tests for current file
- Click diamond in gutter: Run single test

## CI/CD Integration

Tests run automatically:
- Pre-commit: Unit tests (via lefthook)
- Pre-push: All tests
- Pull request: Full test suite + coverage report
- Before release: All tests including UI tests

## Coverage Goals

- ViewModels: 90%+
- Use Cases: 90%+
- Repositories: 80%+
- Utilities: 80%+
- Views: Not measured (tested via UI tests)
- Overall: 70%+
