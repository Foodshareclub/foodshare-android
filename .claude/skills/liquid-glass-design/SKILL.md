---
name: liquid-glass-design
description: Enforce Liquid Glass design system for Foodshare iOS. Use when creating UI components, reviewing designs, or ensuring visual consistency. Requires Atomic → Molecular → Organism hierarchy and design tokens.
---

<objective>
All Foodshare UI must use the Liquid Glass design system. No raw SwiftUI, no custom colors, no hardcoded spacing.
</objective>

<essential_principles>
## Design Philosophy

**"Glass isn't decoration. It's communication."**

- **Transparent** → Honesty. We show what's beneath
- **Layered** → Hierarchy. Information has depth
- **Fluid** → Motion. Transitions guide understanding

## Component Hierarchy (Non-Negotiable)

```
Atomic → Molecular → Organism
```

**Atomic:** Single indivisible elements
- GlassButton, GlassTextField, GlassBadge, GlassIcon, GlassToggle

**Molecular:** Combinations of atomic
- GlassCard, GlassAlert, GlassSearchBar, GlassSegmentedControl

**Organism:** Complete features
- FoodItemCard, GlassLoadingView, GlassEmptyState, NavigationBar

## Mandatory Design Tokens

**Colors (NEVER use Color.blue, etc.):**
```swift
Color.DesignSystem.primary
Color.DesignSystem.background
Color.DesignSystem.glassBackground
Color.DesignSystem.textPrimary
Color.DesignSystem.textSecondary
Color.DesignSystem.success / .warning / .error
```

**Typography (NEVER use .title, .body, etc.):**
```swift
Font.DesignSystem.displayLarge / .displayMedium
Font.DesignSystem.headlineLarge / .headlineMedium
Font.DesignSystem.bodyLarge / .bodyMedium / .bodySmall
Font.DesignSystem.caption
```

**Spacing (NEVER use raw numbers):**
```swift
Spacing.xs  // 4pt
Spacing.sm  // 8pt
Spacing.md  // 16pt
Spacing.lg  // 24pt
Spacing.xl  // 32pt
```

**Corner Radius:**
```swift
CornerRadius.small   // 8pt
CornerRadius.medium  // 16pt
CornerRadius.large   // 24pt
CornerRadius.full    // Pill shape
```

## Red Flags (Instant Violations)

```swift
// ❌ VIOLATIONS
Color.blue                    // Use Color.DesignSystem.primary
Font.title                    // Use Font.DesignSystem.headlineLarge
.padding(16)                  // Use .padding(Spacing.md)
.cornerRadius(12)             // Use .cornerRadius(CornerRadius.medium)
Button("Label") { }           // Use GlassButton("Label") { }
TextField("", text: $text)    // Use GlassTextField("", text: $text)
```
</essential_principles>

<intake>
What design task do you need help with?

1. **Create component** - Build a new UI component
2. **Review UI** - Check for design system violations
3. **Apply styling** - Add glass effects to existing views
4. **Accessibility** - Ensure a11y compliance
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "create", "build", "new component" | workflows/create-component.md |
| 2, "review", "check", "violations" | workflows/review-ui.md |
| 3, "style", "glass", "effects" | workflows/apply-styling.md |
| 4, "accessibility", "a11y", "accessible" | workflows/accessibility.md |
</routing>

<quick_reference>
## Glass Effect Modifier

```swift
.glassEffect()  // Standard glass background
.glassEffect(blur: 30, opacity: 0.9)  // Custom intensity
```

## Standard Components

```swift
// Buttons
GlassButton("Primary Action", style: .primary) { action() }
GlassButton("Secondary", icon: "star", style: .secondary) { }
GlassButton("Tertiary", style: .tertiary) { }

// Text Fields
GlassTextField("Placeholder", text: $text, icon: "magnifyingglass")
GlassTextField("Password", text: $password, isSecure: true)

// Cards
GlassCard {
    VStack(spacing: Spacing.sm) {
        Text("Title").font(.DesignSystem.headlineMedium)
        Text("Body").font(.DesignSystem.bodyMedium)
    }
}

// Badges
GlassBadge(text: "New", style: .info)
GlassBadge(text: "Urgent", style: .warning)

// States
GlassLoadingView()
GlassEmptyState(icon: "tray", title: "Empty", message: "No items")
GlassErrorView(error: error, retry: { })
```

## Animation Tokens

```swift
.animation(.glassStandard, value: state)  // 0.3s ease
.animation(.glassQuick, value: state)     // 0.2s ease
.animation(.glassSlow, value: state)      // 0.5s ease
.animation(.glassSpring, value: state)    // spring response
```

## Accessibility Requirements

```swift
// Minimum touch targets
.frame(minWidth: 44, minHeight: 44)

// Dynamic Type support
.font(.DesignSystem.bodyLarge)  // ✅ Scales with settings
.font(.system(size: 16))        // ❌ Fixed, doesn't scale

// Color contrast
// textPrimary on background: 7:1 ✅
// textSecondary on background: 4.5:1 ✅
```
</quick_reference>

<success_criteria>
UI is design-compliant when:
- [ ] Uses only Color.DesignSystem colors
- [ ] Uses only Font.DesignSystem typography
- [ ] Uses only Spacing tokens for padding/spacing
- [ ] All interactive elements ≥44pt touch targets
- [ ] All text supports Dynamic Type
- [ ] All colors pass WCAG contrast requirements
- [ ] Component follows Atomic → Molecular → Organism hierarchy
- [ ] Animations use standard animation tokens
</success_criteria>
