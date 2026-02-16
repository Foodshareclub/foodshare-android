# PR Review Workflow

<required_reading>
- references/review-checklist.md
</required_reading>

<process>
## Step 1: Get PR Context

**Read the PR description:**
- What is the purpose?
- What issue does it close?
- Any special testing instructions?

**Get the diff:**
```bash
# View PR files
gh pr diff {number}

# Or list changed files
gh pr view {number} --json files --jq '.files[].path'
```

## Step 2: Understand the Scope

Categorize the changes:
- **New feature**: Need full architecture review
- **Bug fix**: Focus on correctness and edge cases
- **Refactor**: Focus on maintaining behavior
- **Dependency update**: Focus on breaking changes

## Step 3: Architecture Review

For each file, verify:

**Domain Layer Files:**
```swift
// Check: No infrastructure imports
import Foundation  // ✅ OK
import Supabase    // ❌ VIOLATION
```

**Presentation Layer Files:**
```swift
// Check: ViewModel has @MainActor
@MainActor           // ✅ Required
@Observable
final class XViewModel { }

// Check: No direct service calls
let data = try await supabase.from("x").select()  // ❌ VIOLATION
let data = try await repository.fetch()           // ✅ OK
```

## Step 4: Security Review

**Check for secrets:**
```bash
# Look for hardcoded keys/secrets
git diff HEAD~1 | grep -i "sk-\|secret\|password\|api.key"
```

**Check for force unwraps:**
```bash
# Force unwraps in non-test files
git diff HEAD~1 -- "*.swift" | grep "!" | grep -v "Tests"
```

**If DB changes, check RLS:**
```sql
-- Every new table needs RLS
ALTER TABLE new_table ENABLE ROW LEVEL SECURITY;
-- And appropriate policies
```

## Step 5: Design System Review

**Check colors:**
```swift
Color.blue        // ❌ Use Color.DesignSystem.primary
Color.red         // ❌ Use Color.DesignSystem.error
.foregroundColor(.gray)  // ❌ Use Color.DesignSystem.textSecondary
```

**Check fonts:**
```swift
.font(.title)     // ❌ Use Font.DesignSystem.headlineLarge
.font(.system(size: 16))  // ❌ Use Font.DesignSystem.bodyLarge
```

**Check spacing:**
```swift
.padding(16)      // ❌ Use .padding(Spacing.md)
```

## Step 6: Performance Review

**Check list implementations:**
```swift
VStack { ForEach... }       // ❌ Use LazyVStack
HStack { ForEach... }       // ❌ Use LazyHStack
```

**Check for expensive operations:**
```swift
.blur(radius: 50)           // ⚠️ Expensive
.shadow(radius: 20)         // ⚠️ Expensive
.animation(.default)        // ❌ Use explicit value
```

## Step 7: Test Review

**Coverage check:**
- Does ViewModel have tests?
- Are error cases tested?
- Are edge cases covered?

**Mock quality:**
- Do mocks track call counts?
- Can mocks stub return values?
- Can mocks simulate errors?

## Step 8: Write Review Summary

```markdown
## PR Review: #{number} - {title}

### Summary
{Overall assessment}

### ✅ Good
- {Positive observation 1}
- {Positive observation 2}

### 🔴 Blocking Issues
1. **{Issue}** - `file.swift:123`
   - {Description}
   - Suggested fix: {solution}

### 🟠 Should Fix
1. **{Issue}** - `file.swift:456`
   - {Description}

### 🔵 Suggestions
1. {Suggestion}

### Decision
- [ ] Approve
- [ ] Request Changes
- [ ] Comment Only
```
</process>

<success_criteria>
PR review complete when:
- [ ] All changed files examined
- [ ] Architecture compliance verified
- [ ] Security scan completed
- [ ] Design system checked
- [ ] Performance considered
- [ ] Tests reviewed
- [ ] Clear feedback provided with line numbers
</success_criteria>
