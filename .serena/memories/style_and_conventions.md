# Foodshare Swift Style & Conventions

## Naming
- **Types**: PascalCase (`FoodListing`, `AuthenticationViewModel`)
- **Variables/Functions**: camelCase (`foodItems`, `fetchListings()`)
- **Constants**: Use `Constants` enum in `Core/Utilities/Constants.swift`
- **Booleans**: Use is/has/should prefix (`isAvailable`, `hasExpired`)

## File Organization
1. Imports
2. Type definition
3. Properties (grouped: @State, @Environment, injected)
4. Initializer
5. Body (for Views)
6. Private computed properties
7. Private methods
8. Extensions

## SwiftUI Patterns
- Use `@Observable` for ViewModels (not ObservableObject)
- Mark ViewModels with `@MainActor`
- Use `@Bindable` for bindings to @Observable
- Break complex views into private computed properties

## Design System (CRITICAL)
All UI MUST use Liquid Glass design system:
- Colors: `Color.DesignSystem.primary`, `.background`, etc.
- Fonts: `Font.DesignSystem.displayLarge`, `.bodyLarge`, etc.
- Spacing: `Spacing.xs` (4pt), `.sm` (8pt), `.md` (16pt), `.lg` (24pt)
- Components: `GlassButton`, `GlassCard`, `GlassTextField`

## Forbidden
- ❌ Raw SwiftUI Button, TextField without design system
- ❌ Color.blue, Color.red (use design system colors)
- ❌ Font.title, Font.body (use design system fonts)
- ❌ Hardcoded spacing values

## Architecture Rules
1. Features are independent (no cross-feature imports)
2. All features depend on Core only
3. Each feature: Domain → Data → Presentation layers
4. Use protocol-oriented programming

## Testing
- 70%+ coverage target
- Use Swift Testing framework
- Use fixtures for test data
- Mock external dependencies
