# Create Feature Workflow

<required_reading>
Before proceeding, read:
- references/layer-rules.md
- templates/feature-scaffold.md
</required_reading>

<process>
## Step 1: Gather Requirements

Ask for:
- Feature name (e.g., "Favorites", "Reports", "Notifications")
- Core entity (what data does it manage?)
- Key operations (list, create, update, delete, search?)
- Supabase table name (if applicable)

## Step 2: Create Domain Layer

```
Features/{FeatureName}/Domain/
├── Models/{Entity}.swift
├── UseCases/{Operation}UseCase.swift
└── Repositories/{Entity}Repository.swift (protocol)
```

**Models:** Pure Swift, no Codable, value semantics
**Use Cases:** Single responsibility, one operation each
**Repository Protocol:** Async throws, Sendable

## Step 3: Create Data Layer

```
Features/{FeatureName}/Data/
├── DTOs/{Entity}DTO.swift
├── Repositories/Supabase{Entity}Repository.swift
└── Mappers/{Entity}Mapper.swift (optional)
```

**DTOs:** Match Supabase table columns exactly, Codable
**Repository:** Implements domain protocol, handles API calls
**Mappers:** Convert DTO ↔ Domain (if complex)

## Step 4: Create Presentation Layer

```
Features/{FeatureName}/Presentation/
├── ViewModels/{Feature}ViewModel.swift
└── Views/{Feature}View.swift
```

**ViewModel:** @Observable, @MainActor, injected dependencies
**View:** Pure UI, calls ViewModel methods only

## Step 5: Wire Up Dependencies

Create factory or use dependency container to inject:
- Repository implementation → Use Case
- Use Case → ViewModel
- ViewModel → View

## Step 6: Verify

Run through success criteria:
- [ ] Domain has no infrastructure imports
- [ ] ViewModel doesn't know about Supabase
- [ ] View has no async logic
- [ ] All protocols in Domain layer
</process>

<success_criteria>
Feature is complete when:
- All three layers exist with proper separation
- Unit tests exist for ViewModel and Use Cases
- Code compiles with no warnings
- Can swap Supabase for mock repository without touching Domain/Presentation
</success_criteria>
