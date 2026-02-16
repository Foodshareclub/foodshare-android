---
name: foodshare-architecture
description: Enforce Clean Architecture patterns for Foodshare iOS. Use when creating features, reviewing code, or diagnosing layer violations. Ensures Domain → Data → Presentation separation with proper dependency injection.
---

<objective>
Ensure all Foodshare code follows Clean Architecture principles with strict layer separation, testability, and maintainability.
</objective>

<essential_principles>
## Core Architecture Rules (Non-Negotiable)

**Layer Direction:** Presentation → Domain ← Data

1. **Domain Layer** (Pure Swift)
   - Models, Use Cases, Repository Protocols
   - NO imports of UIKit, SwiftUI, Supabase, or any infrastructure
   - Business logic lives here exclusively

2. **Data Layer** (Implementations)
   - Repository implementations (SupabaseXxxRepository)
   - DTOs and mappers
   - Can import infrastructure, implements domain protocols

3. **Presentation Layer** (UI)
   - SwiftUI Views and @Observable ViewModels
   - NO business logic, NO direct service calls
   - Depends only on Domain layer

**Red Flags (Instant Violations):**
- ViewModel imports Supabase → WRONG
- Domain model conforms to Codable → WRONG
- View contains async/await network calls → WRONG
- Feature imports another feature → WRONG
</essential_principles>

<intake>
What do you need help with?

1. **Create new feature** - Full layer scaffolding
2. **Review code** - Check for architecture violations
3. **Refactor existing** - Fix layer violations
4. **Add component** - Single layer addition (model, use case, repository, etc.)
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "create", "new feature", "scaffold" | workflows/create-feature.md |
| 2, "review", "check", "audit" | workflows/review-architecture.md |
| 3, "refactor", "fix", "violations" | workflows/refactor-layers.md |
| 4, "add", "component", "single" | workflows/add-component.md |
</routing>

<quick_reference>
## File Organization

```
Features/{FeatureName}/
├── Domain/
│   ├── Models/          # Pure Swift structs
│   ├── UseCases/        # Business operations
│   └── Repositories/    # Protocols only
├── Data/
│   ├── DTOs/            # API response structs (Codable)
│   ├── Repositories/    # Supabase implementations
│   └── Mappers/         # DTO → Domain conversions
└── Presentation/
    ├── ViewModels/      # @Observable @MainActor
    └── Views/           # SwiftUI views
```

## ViewModel Template

```swift
@MainActor
@Observable
final class {Name}ViewModel {
    // State
    var items: [Item] = []
    var isLoading = false
    var error: AppError?

    // Dependencies (injected)
    private let useCase: {Name}UseCase

    init(useCase: {Name}UseCase) {
        self.useCase = useCase
    }

    func load() async {
        guard !isLoading else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            items = try await useCase.execute()
        } catch {
            self.error = AppError.from(error)
        }
    }
}
```

## Repository Protocol Template

```swift
protocol {Name}Repository: Sendable {
    func fetch() async throws -> [Item]
    func save(_ item: Item) async throws
    func delete(id: UUID) async throws
}
```
</quick_reference>

<success_criteria>
Architecture is correct when:
- [ ] Domain layer has zero infrastructure imports
- [ ] ViewModels receive dependencies via init (no global singletons)
- [ ] Views only call ViewModel methods, no async logic
- [ ] Features don't import other features
- [ ] All repository protocols are in Domain, implementations in Data
- [ ] DTOs are separate from domain models
</success_criteria>
