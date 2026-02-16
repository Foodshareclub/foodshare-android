---
inclusion: always
---

# Swift Code Style Guide

## General Principles

- Write clear, self-documenting code
- Prefer composition over inheritance
- Use protocol-oriented programming
- Leverage Swift's type system for safety
- Follow Swift API Design Guidelines

## Naming Conventions

### Types
```swift
// PascalCase for types
struct FoodListing { }
class AuthenticationViewModel { }
protocol FoodListingRepository { }
enum ListingStatus { }
```

### Variables & Functions
```swift
// camelCase for variables and functions
let foodItems: [FoodItem]
var isLoading = false
func fetchListings() async throws { }
```

### Constants
```swift
// Use the centralized Constants enum in Core/Utilities/Constants.swift
Constants.bundleIdentifier      // "com.flutterflow.foodshare"
Constants.appName               // "Foodshare"
Constants.maxImageCount         // 5
Constants.defaultSearchRadius   // 5.0 km
Constants.maxImageSize          // 5MB

// For feature-specific constants, use local enums
enum ListingConstants {
    static let minTitleLength = 3
    static let maxDescriptionLength = 1000
}
```

### Boolean Properties
```swift
// Use is/has/should prefix
var isAvailable: Bool
var hasExpired: Bool
var shouldShowError: Bool
```

## Code Organization

### File Structure
```swift
// 1. Imports
import SwiftUI
import Supabase

// 2. Type definition
struct FoodListingView: View {
    // 3. Properties (grouped by type)
    // State properties
    @State private var listings: [FoodListing] = []
    @State private var isLoading = false
    
    // Environment properties
    @Environment(\.supabase) private var supabase
    
    // Injected dependencies
    private let viewModel: FoodListingViewModel
    
    // 4. Initializer
    init(viewModel: FoodListingViewModel) {
        self.viewModel = viewModel
    }
    
    // 5. Body
    var body: some View {
        // View code
    }
    
    // 6. Private computed properties
    private var filteredListings: [FoodListing] {
        // Filter logic
    }
    
    // 7. Private methods
    private func loadListings() async {
        // Load logic
    }
}

// 8. Extensions
extension FoodListingView {
    // Preview provider, etc.
}
```

## SwiftUI Patterns

### View Composition
```swift
// Break down complex views into smaller components
struct FoodListingCard: View {
    let listing: FoodListing
    
    var body: some View {
        VStack(alignment: .leading, spacing: Spacing.sm) {
            listingImage
            listingDetails
            listingFooter
        }
    }
    
    private var listingImage: some View {
        AsyncImage(url: listing.primaryImageUrl) { image in
            image.resizable().aspectRatio(contentMode: .fill)
        } placeholder: {
            ProgressView()
        }
    }
    
    private var listingDetails: some View {
        VStack(alignment: .leading, spacing: Spacing.xs) {
            Text(listing.title)
                .font(.DesignSystem.headlineMedium)
            Text(listing.description ?? "")
                .font(.DesignSystem.bodySmall)
                .foregroundColor(.DesignSystem.textSecondary)
        }
    }
    
    private var listingFooter: some View {
        HStack {
            Text(listing.distance)
            Spacer()
            Text(listing.expiryDate, style: .relative)
        }
        .font(.DesignSystem.caption)
    }
}
```

### State Management
```swift
// Use @Observable for ViewModels (iOS 17+)
// Mark with @MainActor for UI state management
@MainActor
@Observable
final class FeedViewModel {
    // MARK: - Properties
    var listings: [FoodListing] = []
    var categories: [FoodCategory] = []
    var selectedCategory: FoodCategory?
    var isLoading = false
    var isRefreshing = false
    var error: AppError?
    var showError = false
    var searchRadius = 5.0 // km
    
    private let fetchListingsUseCase: FetchListingsUseCase
    private let fetchCategoriesUseCase: FetchCategoriesUseCase
    private let locationService: LocationService
    
    // MARK: - Computed Properties
    var filteredListings: [FoodListing] {
        guard let selectedCategory else { return listings }
        return listings.filter { $0.categoryId == selectedCategory.id }
    }
    
    var hasListings: Bool {
        !listings.isEmpty
    }
    
    // MARK: - Initialization
    init(
        fetchListingsUseCase: FetchListingsUseCase,
        fetchCategoriesUseCase: FetchCategoriesUseCase,
        locationService: LocationService
    ) {
        self.fetchListingsUseCase = fetchListingsUseCase
        self.fetchCategoriesUseCase = fetchCategoriesUseCase
        self.locationService = locationService
    }
    
    // MARK: - Actions
    
    /// Load initial data
    func loadInitialData() async {
        await loadCategories()
        await loadListings()
    }
    
    /// Load food listings
    func loadListings() async {
        guard !isLoading else { return }
        
        isLoading = true
        error = nil
        showError = false
        defer { isLoading = false }
        
        do {
            let location = try await locationService.getCurrentLocation()
            listings = try await fetchListingsUseCase.execute(
                near: location,
                radius: searchRadius,
                categoryId: selectedCategory?.id,
                limit: 50
            )
        } catch let appError as AppError {
            error = appError
            showError = true
        } catch {
            self.error = .networkError(error.localizedDescription)
            showError = true
        }
    }
    
    /// Refresh listings
    func refresh() async {
        guard !isRefreshing else { return }
        isRefreshing = true
        defer { isRefreshing = false }
        await loadListings()
    }
}

// Access in views using @State (not @EnvironmentObject)
struct FeedView: View {
    @State private var viewModel: FeedViewModel
    
    init(viewModel: FeedViewModel) {
        _viewModel = State(initialValue: viewModel)
    }
    
    var body: some View {
        List(viewModel.filteredListings) { listing in
            FoodListingCard(listing: listing)
        }
        .task {
            await viewModel.loadInitialData()
        }
        .refreshable {
            await viewModel.refresh()
        }
        .alert("Error", isPresented: $viewModel.showError) {
            Button("OK") { viewModel.dismissError() }
        } message: {
            Text(viewModel.errorMessage)
        }
    }
}
```

## Async/Await Patterns

### Async Functions
```swift
// Use async/await for asynchronous operations
func fetchListings() async throws -> [FoodListing] {
    let response = try await supabase
        .from("food_items")
        .select()
        .execute()
    return response.value
}
```

### Task Management
```swift
// Use .task for view lifecycle
.task {
    await viewModel.loadListings()
}

// Cancel tasks when view disappears
.task(id: searchQuery) {
    await viewModel.search(query: searchQuery)
}
```

### Actor Isolation
```swift
// Use actors for thread-safe shared state
actor LocationManager {
    private var currentLocation: CLLocation?
    
    func updateLocation(_ location: CLLocation) {
        currentLocation = location
    }
    
    func getCurrentLocation() -> CLLocation? {
        currentLocation
    }
}
```

## Error Handling

### Custom Errors
```swift
enum AppError: LocalizedError {
    case networkError(String)
    case validationError(String)
    case unauthorized
    case notFound
    
    var errorDescription: String? {
        switch self {
        case .networkError(let message):
            return "Network error: \(message)"
        case .validationError(let message):
            return message
        case .unauthorized:
            return "You must be logged in"
        case .notFound:
            return "Resource not found"
        }
    }
}
```

### Error Handling Pattern
```swift
do {
    let listings = try await repository.fetchListings()
    self.listings = listings
} catch let error as AppError {
    self.error = error
} catch {
    self.error = .networkError(error.localizedDescription)
}
```

## Protocol-Oriented Programming

### Repository Pattern
```swift
// Protocol defines interface
protocol FoodListingRepository: Sendable {
    func fetchListings(near location: Location, radius: Double) async throws -> [FoodListing]
    func createListing(_ listing: FoodListing) async throws -> FoodListing
}

// Concrete implementation
final class SupabaseFoodListingRepository: FoodListingRepository {
    private let supabase: SupabaseClient
    
    init(supabase: SupabaseClient) {
        self.supabase = supabase
    }
    
    func fetchListings(near location: Location, radius: Double) async throws -> [FoodListing] {
        // Implementation
    }
}

// Mock for testing
final class MockFoodListingRepository: FoodListingRepository {
    var mockListings: [FoodListing] = []
    
    func fetchListings(near location: Location, radius: Double) async throws -> [FoodListing] {
        return mockListings
    }
}
```

## Comments & Documentation

### When to Comment
```swift
// ✅ Good: Explain WHY, not WHAT
// Calculate distance using Haversine formula for accuracy
let distance = calculateDistance(from: userLocation, to: listingLocation)

// ❌ Bad: Obvious comment
// Set isLoading to true
isLoading = true
```

### Documentation Comments
```swift
/// Fetches food listings within the specified radius of a location.
///
/// - Parameters:
///   - location: The center point for the search
///   - radius: Search radius in kilometers (1-100)
/// - Returns: Array of food listings sorted by distance
/// - Throws: `AppError.networkError` if the request fails
func fetchListings(near location: Location, radius: Double) async throws -> [FoodListing]
```

## SwiftLint Rules

Key rules enforced in `.swiftlint.yml`:

- Line length: 120 characters (warning), 200 (error)
- Identifier names: min 2 characters (except id, url, db)
- Force unwrapping: Avoid `!` except in tests
- Trailing whitespace: Not allowed
- Empty count: Use `.isEmpty` instead of `.count == 0`
- Explicit init: Prefer explicit initialization

## Best Practices

### Optionals
```swift
// ✅ Use optional binding
if let listing = selectedListing {
    showDetail(listing)
}

// ✅ Use nil coalescing
let title = listing.title ?? "Untitled"

// ❌ Avoid force unwrapping
let title = listing.title! // Don't do this
```

### Collections
```swift
// ✅ Use isEmpty
if listings.isEmpty { }

// ❌ Don't compare count to zero
if listings.count == 0 { }
```

### Type Inference
```swift
// ✅ Let Swift infer when obvious
let listings = [FoodListing]()
let title = "Fresh Apples"

// ✅ Be explicit when needed
let distance: Double = 5.0
let status: ListingStatus = .available
```

### Access Control
```swift
// Use private for internal implementation
private func validateInput() { }

// Use fileprivate sparingly
fileprivate func helperMethod() { }

// Default to internal (no modifier)
func publicMethod() { }
```

### Extensions
```swift
// Group related functionality
extension FoodListing {
    var isExpired: Bool {
        guard let expiryDate else { return false }
        return Date() > expiryDate
    }
    
    var isAvailable: Bool {
        status == .available && !isExpired
    }
}

// Separate protocol conformance
extension FoodListing: Equatable {
    static func == (lhs: FoodListing, rhs: FoodListing) -> Bool {
        lhs.id == rhs.id
    }
}
```
