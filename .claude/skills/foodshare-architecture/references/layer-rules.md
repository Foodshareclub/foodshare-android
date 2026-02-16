# Clean Architecture Layer Rules

## Layer Dependency Direction

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│   (Views, ViewModels)               │
│                                     │
│   Can import: Domain, Core/Design   │
│   Cannot import: Data, Supabase     │
└────────────────┬────────────────────┘
                 │ depends on
                 ▼
┌─────────────────────────────────────┐
│           Domain Layer              │
│   (Models, Use Cases, Protocols)    │
│                                     │
│   Can import: Foundation only       │
│   Cannot import: ANYTHING ELSE      │
└────────────────▲────────────────────┘
                 │ implements
                 │
┌────────────────┴────────────────────┐
│            Data Layer               │
│   (DTOs, Repositories, Mappers)     │
│                                     │
│   Can import: Domain, Supabase      │
│   Cannot import: Presentation       │
└─────────────────────────────────────┘
```

## Domain Layer Rules

**Purpose:** Business logic and entities, completely framework-agnostic.

**Allowed:**
- `import Foundation`
- Pure Swift types (structs, enums, protocols)
- Value types preferred over reference types

**Forbidden:**
- `import Supabase`
- `import SwiftUI`
- `import UIKit`
- `Codable` conformance (use DTOs instead)
- Any external framework imports

**Contents:**
- **Models:** Core business entities
- **Use Cases:** Single-operation business logic
- **Repository Protocols:** Abstract data access

## Data Layer Rules

**Purpose:** Implement domain protocols with concrete infrastructure.

**Allowed:**
- `import Foundation`
- `import Supabase`
- Domain layer imports
- `Codable` conformance (for DTOs)

**Forbidden:**
- `import SwiftUI`
- `import UIKit`
- Presentation layer imports

**Contents:**
- **DTOs:** API response/request structures
- **Repositories:** Concrete implementations
- **Mappers:** DTO ↔ Domain conversions

## Presentation Layer Rules

**Purpose:** UI and user interaction, delegates business logic.

**Allowed:**
- `import SwiftUI`
- `import Foundation`
- Domain layer imports
- Core/Design imports

**Forbidden:**
- `import Supabase`
- Data layer imports
- Direct infrastructure access

**Contents:**
- **ViewModels:** UI state and actions
- **Views:** SwiftUI UI components

## Practical Examples

### Correct: Domain Model
```swift
// Features/Feed/Domain/Models/FoodListing.swift
import Foundation

struct FoodListing: Identifiable, Equatable, Sendable {
    let id: UUID
    let title: String
    let description: String
    let category: ListingCategory
    let location: Coordinate
    let createdAt: Date

    var isExpired: Bool {
        // Business logic belongs here
        expiryDate < Date()
    }
}
```

### Correct: Repository Protocol
```swift
// Features/Feed/Domain/Repositories/FoodListingRepository.swift
import Foundation

protocol FoodListingRepository: Sendable {
    func fetchNearby(location: Coordinate, radiusKm: Double) async throws -> [FoodListing]
    func fetch(id: UUID) async throws -> FoodListing?
    func create(_ listing: FoodListing) async throws -> FoodListing
    func delete(id: UUID) async throws
}
```

### Correct: DTO
```swift
// Features/Feed/Data/DTOs/FoodListingDTO.swift
import Foundation

struct FoodListingDTO: Codable, Sendable {
    let id: String
    let title: String
    let description: String?
    let category: String
    let latitude: Double
    let longitude: Double
    let created_at: String

    func toDomain() -> FoodListing {
        FoodListing(
            id: UUID(uuidString: id) ?? UUID(),
            title: title,
            description: description ?? "",
            category: ListingCategory(rawValue: category) ?? .other,
            location: Coordinate(latitude: latitude, longitude: longitude),
            createdAt: ISO8601DateFormatter().date(from: created_at) ?? Date()
        )
    }
}
```

### Correct: Repository Implementation
```swift
// Features/Feed/Data/Repositories/SupabaseFoodListingRepository.swift
import Foundation
import Supabase

final class SupabaseFoodListingRepository: FoodListingRepository, @unchecked Sendable {
    private let client: SupabaseClient

    init(client: SupabaseClient) {
        self.client = client
    }

    func fetchNearby(location: Coordinate, radiusKm: Double) async throws -> [FoodListing] {
        let response: [FoodListingDTO] = try await client
            .rpc("search_food_items_nearby", params: [
                "user_lat": location.latitude,
                "user_lng": location.longitude,
                "radius_meters": radiusKm * 1000
            ])
            .execute()
            .value

        return response.map { $0.toDomain() }
    }
}
```

### Correct: ViewModel
```swift
// Features/Feed/Presentation/ViewModels/FeedViewModel.swift
import Foundation

@MainActor
@Observable
final class FeedViewModel {
    var listings: [FoodListing] = []
    var isLoading = false
    var error: AppError?

    private let repository: FoodListingRepository
    private let locationService: LocationService

    init(repository: FoodListingRepository, locationService: LocationService) {
        self.repository = repository
        self.locationService = locationService
    }

    func loadNearbyListings() async {
        guard !isLoading else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            let location = try await locationService.currentLocation()
            listings = try await repository.fetchNearby(location: location, radiusKm: 5)
        } catch {
            self.error = AppError.from(error)
        }
    }
}
```
