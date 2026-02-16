# Fix Frame Drops Workflow

<required_reading>
- references/proMotion-optimization.md
</required_reading>

<process>
## Step 1: Reproduce and Measure

**Identify the problem:**
- When does it happen? (scroll, animation, interaction)
- How severe? (occasional stutter vs. constant jank)
- Which views are involved?

**Measure with Instruments:**
1. Product → Profile → Core Animation
2. Record during problematic interaction
3. Look for frames > 8.3ms (120fps) or > 16.6ms (60fps)

## Step 2: Common Causes and Fixes

### Cause: VStack with ForEach (not lazy)
```swift
// ❌ Problem: Creates all views immediately
VStack {
    ForEach(items) { item in
        ItemCard(item: item)
    }
}

// ✅ Fix: Use LazyVStack
LazyVStack {
    ForEach(items) { item in
        ItemCard(item: item)
    }
}
```

### Cause: Complex View Hierarchy
```swift
// ❌ Problem: Deep hierarchy, expensive composition
ZStack {
    ForEach(0..<100) { i in
        Circle()
            .blur(radius: CGFloat(i))
    }
}

// ✅ Fix: Use drawingGroup() to flatten
ZStack {
    ForEach(0..<100) { i in
        Circle()
            .blur(radius: CGFloat(i))
    }
}
.drawingGroup()  // Renders to single Metal texture
```

### Cause: Excessive Blur/Shadow
```swift
// ❌ Problem: GPU intensive effects on every cell
ForEach(items) { item in
    ItemCard(item: item)
        .blur(radius: 20)
        .shadow(radius: 15)
}

// ✅ Fix: Reduce/remove or apply to container
ItemCard(item: item)
    .background(
        RoundedRectangle(cornerRadius: 12)
            .fill(.ultraThinMaterial)  // Built-in, optimized
    )
```

### Cause: Implicit Animation
```swift
// ❌ Problem: Animates everything, expensive
.animation(.default)

// ✅ Fix: Explicit value tracking
.animation(.smooth(duration: 0.3), value: isExpanded)
```

### Cause: Synchronous Image Loading
```swift
// ❌ Problem: Blocks main thread
Image(uiImage: UIImage(data: imageData)!)

// ✅ Fix: Use Kingfisher with async loading
KFImage(imageURL)
    .placeholder { ProgressView() }
    .fade(duration: 0.25)
    .resizable()
```

### Cause: Main Thread Work
```swift
// ❌ Problem: Computation on main thread
Button("Process") {
    let result = expensiveComputation()  // Blocks UI
    self.result = result
}

// ✅ Fix: Move to background
Button("Process") {
    Task.detached(priority: .userInitiated) {
        let result = expensiveComputation()
        await MainActor.run {
            self.result = result
        }
    }
}
```

## Step 3: Profile After Fix

1. Rebuild and profile again
2. Compare frame times before/after
3. Target: < 8.3ms for 120fps

## Step 4: Monitor

Add signposts for ongoing monitoring:
```swift
import os.signpost

let log = OSLog(subsystem: "Foodshare", category: "Performance")

func loadFeed() async {
    os_signpost(.begin, log: log, name: "LoadFeed")
    defer { os_signpost(.end, log: log, name: "LoadFeed") }

    // ... load logic
}
```
</process>

<success_criteria>
Frame drops fixed when:
- [ ] Scroll is smooth at 120fps
- [ ] No visible stutter during interactions
- [ ] Frame time consistently < 8.3ms
- [ ] No regression in other areas
</success_criteria>
