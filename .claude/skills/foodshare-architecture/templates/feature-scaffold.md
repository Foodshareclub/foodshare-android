# Feature Scaffold Template

Replace `{FeatureName}`, `{Entity}`, `{entity}` with actual names.

## Directory Structure

```
Features/{FeatureName}/
├── Domain/
│   ├── Models/
│   │   └── {Entity}.swift
│   ├── UseCases/
│   │   ├── Fetch{Entity}UseCase.swift
│   │   ├── Create{Entity}UseCase.swift
│   │   └── Delete{Entity}UseCase.swift
│   └── Repositories/
│       └── {Entity}Repository.swift
├── Data/
│   ├── DTOs/
│   │   └── {Entity}DTO.swift
│   └── Repositories/
│       └── Supabase{Entity}Repository.swift
└── Presentation/
    ├── ViewModels/
    │   └── {FeatureName}ViewModel.swift
    └── Views/
        ├── {FeatureName}View.swift
        └── {Entity}DetailView.swift
```

## Domain Layer Templates

### Model
```swift
// Features/{FeatureName}/Domain/Models/{Entity}.swift
import Foundation

struct {Entity}: Identifiable, Equatable, Sendable, Hashable {
    let id: UUID
    // Add properties here
    let createdAt: Date
    let updatedAt: Date
}
```

### Repository Protocol
```swift
// Features/{FeatureName}/Domain/Repositories/{Entity}Repository.swift
import Foundation

protocol {Entity}Repository: Sendable {
    func fetchAll() async throws -> [{Entity}]
    func fetch(id: UUID) async throws -> {Entity}?
    func create(_ {entity}: {Entity}) async throws -> {Entity}
    func update(_ {entity}: {Entity}) async throws -> {Entity}
    func delete(id: UUID) async throws
}
```

### Use Case
```swift
// Features/{FeatureName}/Domain/UseCases/Fetch{Entity}UseCase.swift
import Foundation

final class Fetch{Entity}UseCase: Sendable {
    private let repository: {Entity}Repository

    init(repository: {Entity}Repository) {
        self.repository = repository
    }

    func execute() async throws -> [{Entity}] {
        try await repository.fetchAll()
    }
}
```

## Data Layer Templates

### DTO
```swift
// Features/{FeatureName}/Data/DTOs/{Entity}DTO.swift
import Foundation

struct {Entity}DTO: Codable, Sendable {
    let id: String
    // Match Supabase column names (snake_case)
    let created_at: String
    let updated_at: String

    func toDomain() -> {Entity} {
        {Entity}(
            id: UUID(uuidString: id) ?? UUID(),
            createdAt: ISO8601DateFormatter().date(from: created_at) ?? Date(),
            updatedAt: ISO8601DateFormatter().date(from: updated_at) ?? Date()
        )
    }

    static func fromDomain(_ {entity}: {Entity}) -> {Entity}DTO {
        {Entity}DTO(
            id: {entity}.id.uuidString,
            created_at: ISO8601DateFormatter().string(from: {entity}.createdAt),
            updated_at: ISO8601DateFormatter().string(from: {entity}.updatedAt)
        )
    }
}
```

### Repository Implementation
```swift
// Features/{FeatureName}/Data/Repositories/Supabase{Entity}Repository.swift
import Foundation
import Supabase

final class Supabase{Entity}Repository: {Entity}Repository, @unchecked Sendable {
    private let client: SupabaseClient
    private let tableName = "{table_name}" // Supabase table name

    init(client: SupabaseClient) {
        self.client = client
    }

    func fetchAll() async throws -> [{Entity}] {
        let response: [{Entity}DTO] = try await client
            .from(tableName)
            .select()
            .order("created_at", ascending: false)
            .execute()
            .value

        return response.map { $0.toDomain() }
    }

    func fetch(id: UUID) async throws -> {Entity}? {
        let response: [{Entity}DTO] = try await client
            .from(tableName)
            .select()
            .eq("id", value: id.uuidString)
            .limit(1)
            .execute()
            .value

        return response.first?.toDomain()
    }

    func create(_ {entity}: {Entity}) async throws -> {Entity} {
        let dto = {Entity}DTO.fromDomain({entity})
        let response: {Entity}DTO = try await client
            .from(tableName)
            .insert(dto)
            .select()
            .single()
            .execute()
            .value

        return response.toDomain()
    }

    func update(_ {entity}: {Entity}) async throws -> {Entity} {
        let dto = {Entity}DTO.fromDomain({entity})
        let response: {Entity}DTO = try await client
            .from(tableName)
            .update(dto)
            .eq("id", value: {entity}.id.uuidString)
            .select()
            .single()
            .execute()
            .value

        return response.toDomain()
    }

    func delete(id: UUID) async throws {
        try await client
            .from(tableName)
            .delete()
            .eq("id", value: id.uuidString)
            .execute()
    }
}
```

## Presentation Layer Templates

### ViewModel
```swift
// Features/{FeatureName}/Presentation/ViewModels/{FeatureName}ViewModel.swift
import Foundation

@MainActor
@Observable
final class {FeatureName}ViewModel {
    // MARK: - State
    var items: [{Entity}] = []
    var selectedItem: {Entity}?
    var isLoading = false
    var error: AppError?

    // MARK: - Dependencies
    private let fetchUseCase: Fetch{Entity}UseCase
    private let createUseCase: Create{Entity}UseCase
    private let deleteUseCase: Delete{Entity}UseCase

    // MARK: - Init
    init(
        fetchUseCase: Fetch{Entity}UseCase,
        createUseCase: Create{Entity}UseCase,
        deleteUseCase: Delete{Entity}UseCase
    ) {
        self.fetchUseCase = fetchUseCase
        self.createUseCase = createUseCase
        self.deleteUseCase = deleteUseCase
    }

    // MARK: - Actions
    func load() async {
        guard !isLoading else { return }
        isLoading = true
        defer { isLoading = false }

        do {
            items = try await fetchUseCase.execute()
        } catch {
            self.error = AppError.from(error)
        }
    }

    func create(_ {entity}: {Entity}) async {
        do {
            let created = try await createUseCase.execute({entity})
            items.insert(created, at: 0)
        } catch {
            self.error = AppError.from(error)
        }
    }

    func delete(_ {entity}: {Entity}) async {
        do {
            try await deleteUseCase.execute({entity}.id)
            items.removeAll { $0.id == {entity}.id }
        } catch {
            self.error = AppError.from(error)
        }
    }

    func clearError() {
        error = nil
    }
}
```

### View
```swift
// Features/{FeatureName}/Presentation/Views/{FeatureName}View.swift
import SwiftUI

struct {FeatureName}View: View {
    @State private var viewModel: {FeatureName}ViewModel

    init(viewModel: {FeatureName}ViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        GlassContainer {
            if viewModel.isLoading && viewModel.items.isEmpty {
                GlassLoadingView()
            } else if viewModel.items.isEmpty {
                GlassEmptyState(
                    icon: "tray",
                    title: "No Items",
                    message: "Items will appear here.",
                    action: { Task { await viewModel.load() } }
                )
            } else {
                itemsList
            }
        }
        .task { await viewModel.load() }
        .refreshable { await viewModel.load() }
        .alert(item: $viewModel.error) { error in
            Alert(
                title: Text("Error"),
                message: Text(error.localizedDescription),
                dismissButton: .default(Text("OK")) { viewModel.clearError() }
            )
        }
    }

    private var itemsList: some View {
        ScrollView {
            LazyVStack(spacing: Spacing.md) {
                ForEach(viewModel.items) { item in
                    {Entity}Card(item: item)
                        .onTapGesture { viewModel.selectedItem = item }
                }
            }
            .padding(.horizontal, Spacing.md)
        }
    }
}

#Preview {
    {FeatureName}View(viewModel: .preview)
}
```

## Mock for Previews and Testing

```swift
// Features/{FeatureName}/Mocks/Mock{Entity}Repository.swift
import Foundation

final class Mock{Entity}Repository: {Entity}Repository, @unchecked Sendable {
    var items: [{Entity}] = []
    var shouldThrowError = false
    var errorToThrow: Error = NSError(domain: "Test", code: 0)

    func fetchAll() async throws -> [{Entity}] {
        if shouldThrowError { throw errorToThrow }
        return items
    }

    func fetch(id: UUID) async throws -> {Entity}? {
        if shouldThrowError { throw errorToThrow }
        return items.first { $0.id == id }
    }

    func create(_ {entity}: {Entity}) async throws -> {Entity} {
        if shouldThrowError { throw errorToThrow }
        items.append({entity})
        return {entity}
    }

    func update(_ {entity}: {Entity}) async throws -> {Entity} {
        if shouldThrowError { throw errorToThrow }
        if let index = items.firstIndex(where: { $0.id == {entity}.id }) {
            items[index] = {entity}
        }
        return {entity}
    }

    func delete(id: UUID) async throws {
        if shouldThrowError { throw errorToThrow }
        items.removeAll { $0.id == id }
    }
}
```
