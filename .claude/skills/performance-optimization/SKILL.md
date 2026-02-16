---
name: performance-optimization
description: Optimize Foodshare iOS for 120Hz ProMotion displays and GPU rendering. Use for animations, frame drops, memory issues, profiling, and achieving smooth 120fps performance.
---

<objective>
Deliver butter-smooth 120fps animations on ProMotion displays. Every frame matters. Every millisecond counts.
</objective>

<essential_principles>
## ProMotion Performance Targets

- **Frame time**: < 8.3ms (120fps) or < 16.6ms (60fps)
- **App launch**: < 2 seconds to interactive
- **Scroll performance**: Zero frame drops
- **Memory idle**: < 150MB
- **Animation jank**: Never visible

## Animation Best Practices

```swift
// ✅ Use interpolating spring for 120Hz smoothness
withAnimation(.interpolatingSpring(stiffness: 300, damping: 20)) {
    isExpanded.toggle()
}

// ✅ Explicit value tracking for efficient updates
.animation(.smooth(duration: 0.3), value: selectedItem)

// ✅ TimelineView for frame-perfect animations
TimelineView(.animation(minimumInterval: 1/120)) { timeline in
    // Updates at 120fps on ProMotion
    AnimatedView(date: timeline.date)
}

// ✅ PhaseAnimator for multi-step animations
PhaseAnimator([false, true]) { phase in
    Circle()
        .scaleEffect(phase ? 1.2 : 1.0)
        .opacity(phase ? 0.5 : 1.0)
}

// ❌ AVOID implicit animation (inefficient)
.animation(.default)  // Animates everything
```

## GPU Optimization

```swift
// ✅ Use drawingGroup() for complex views
ComplexGlassView()
    .drawingGroup()  // Flattens to Metal texture

// ✅ Canvas for custom drawing (GPU accelerated)
Canvas { context, size in
    let path = Path { p in
        p.addEllipse(in: CGRect(origin: .zero, size: size))
    }
    context.fill(path, with: .color(.blue))
}

// ✅ Prefer Canvas over stacked Path views
// ONE Canvas with 100 shapes > 100 Path views

// ❌ AVOID excessive blur/shadow
.blur(radius: 50)       // Very expensive
.shadow(radius: 20)     // Expensive

// ✅ Apply blur sparingly, cache when possible
.blur(radius: 10)       // Moderate
.drawingGroup()         // Then flatten
```

## List Performance

```swift
// ✅ ALWAYS use Lazy containers for lists
LazyVStack(spacing: 16) {
    ForEach(items) { item in
        ItemCard(item: item)
    }
}

// ❌ NEVER use VStack for dynamic lists
VStack {  // Creates ALL views immediately
    ForEach(items) { item in
        ItemCard(item: item)
    }
}

// ✅ Use id parameter for stable identity
ForEach(items, id: \.id) { item in
    ItemCard(item: item)
}

// ✅ Prefetch images with Kingfisher
KFImage(url)
    .fade(duration: 0.25)  // Smooth transition
    .resizable()
    .aspectRatio(contentMode: .fill)
```

## Memory Management

```swift
// ✅ Use weak self in closures that outlive self
Task { [weak self] in
    let data = await fetch()
    await MainActor.run {
        self?.items = data  // Safe if self deallocated
    }
}

// ✅ Cancel tasks when view disappears
.task(id: searchQuery) {
    await search(searchQuery)
}  // Automatically cancelled on disappear/id change

// ✅ Limit image cache size
let cache = ImageCache.default
cache.memoryStorage.config.totalCostLimit = 100 * 1024 * 1024  // 100MB
cache.diskStorage.config.sizeLimit = 500 * 1024 * 1024  // 500MB
```

## Profiling Tools

```bash
# Instruments - Core Animation (frame rate)
# Instruments - Time Profiler (CPU hotspots)
# Instruments - Allocations (memory growth)
# Instruments - Leaks (memory leaks)

# Xcode - Memory Graph Debugger (retain cycles)
# Xcode - View Debugger (view hierarchy)
```
</essential_principles>

<intake>
What performance issue do you need help with?

1. **Frame drops** - Choppy scrolling or animations
2. **Memory issues** - High usage or leaks
3. **Slow launch** - App startup time
4. **Animation jank** - Stuttering transitions
5. **Profile code** - Find performance bottlenecks
</intake>

<routing>
| Response | Workflow |
|----------|----------|
| 1, "frame", "scroll", "choppy", "fps" | workflows/fix-frame-drops.md |
| 2, "memory", "leak", "ram", "crash" | workflows/fix-memory.md |
| 3, "launch", "startup", "slow start" | workflows/improve-launch.md |
| 4, "animation", "jank", "stutter" | workflows/smooth-animations.md |
| 5, "profile", "instruments", "bottleneck" | workflows/profile-code.md |
</routing>

<quick_reference>
## Performance Checklist

Before shipping any UI:
- [ ] Scroll 100+ items without frame drops
- [ ] Animations complete at 120fps
- [ ] Memory stays stable during use
- [ ] No retain cycles (check Memory Graph)
- [ ] Images load progressively with fade
- [ ] List cells recycle properly

## Quick Fixes

**Choppy scroll:**
```swift
// Change VStack → LazyVStack
// Add .drawingGroup() to complex cells
// Remove .animation(.default) modifiers
```

**High memory:**
```swift
// Use @State instead of @StateObject where possible
// Limit Kingfisher cache
// Cancel unused Tasks
```

**Slow launch:**
```swift
// Defer non-critical initialization
// Use lazy properties
// Profile with Time Profiler
```

## Benchmark Commands

```swift
// Measure execution time
let start = CFAbsoluteTimeGetCurrent()
// ... code to measure ...
let elapsed = CFAbsoluteTimeGetCurrent() - start
print("Elapsed: \(elapsed * 1000)ms")

// Signpost for Instruments
import os.signpost
let log = OSLog(subsystem: "Foodshare", category: "Performance")
os_signpost(.begin, log: log, name: "FetchListings")
// ... operation ...
os_signpost(.end, log: log, name: "FetchListings")
```
</quick_reference>

<success_criteria>
Performance is acceptable when:
- [ ] 120fps during normal scroll (8.3ms frame time)
- [ ] < 2 second app launch
- [ ] < 150MB memory idle
- [ ] No frame drops during animations
- [ ] No memory growth over time (no leaks)
- [ ] Images load without blocking UI
</success_criteria>
