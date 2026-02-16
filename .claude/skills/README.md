# Foodshare Claude Code Skills

**10 domain-specific skills for the Foodshare iOS project.**

## 📍 Skill Directory

```
.claude/skills/
├── foodshare-architecture/    # Clean Architecture enforcement
├── foodshare-testing/         # Swift Testing patterns
├── foodshare-deployment/      # Build, test, deploy automation
├── liquid-glass-design/       # Design system compliance
├── supabase-workflow/         # Database and backend
├── swift-concurrency/         # Async/await, actors, Sendable
├── performance-optimization/  # 120Hz ProMotion, GPU, profiling
├── offline-first/             # Core Data sync, offline support
├── feature-development/       # End-to-end feature workflow
└── code-review/               # PR reviews, quality checks
```

## 🎯 When Each Skill Activates

| Skill | Triggers |
|-------|----------|
| **foodshare-architecture** | Creating features, reviewing architecture, layer violations |
| **foodshare-testing** | Writing tests, debugging failures, mocks, coverage |
| **foodshare-deployment** | TestFlight, App Store, CI/CD, build issues |
| **liquid-glass-design** | UI components, design review, styling, accessibility |
| **supabase-workflow** | Migrations, RLS, Edge Functions, queries |
| **swift-concurrency** | Sendable errors, actors, TaskGroups, data races |
| **performance-optimization** | Frame drops, memory, animations, profiling |
| **offline-first** | Core Data, sync, conflicts, optimistic UI |
| **feature-development** | New features from scratch (orchestrates all skills) |
| **code-review** | PR reviews, audits, security scans |

## 🚀 How Skills Work

Skills automatically activate when Claude detects relevant context:

```text
You: "Create a favorites feature"
→ feature-development activates
→ Orchestrates: architecture, supabase, design, testing
→ Complete feature with proper structure
```

```text
You: "Fix this Sendable error"
→ swift-concurrency activates
→ Provides specific fix patterns
```

```text
You: "Review this PR"
→ code-review activates
→ Systematic quality check
```

## 📚 Skill Structure

Each skill uses the router pattern:

```
skill-name/
├── SKILL.md           # Main entry + essential principles
├── workflows/         # Step-by-step procedures
├── references/        # Domain knowledge
└── templates/         # Reusable code patterns
```

**SKILL.md contains:**
- YAML frontmatter (name, description)
- Essential principles (always loaded)
- Intake question (routes to workflows)
- Quick reference (common patterns)
- Success criteria

## 🎓 Learning From Skills

New developers can learn Foodshare patterns by reading skills:

| Skill | Teaches |
|-------|---------|
| foodshare-architecture | Clean Architecture layers |
| foodshare-testing | Swift Testing, TDD |
| liquid-glass-design | Design system patterns |
| swift-concurrency | Swift 6 concurrency |
| performance-optimization | ProMotion optimization |

## 🔧 Customizing Skills

Skills are markdown files. Edit to match evolving patterns:

```bash
# Edit a skill
code .claude/skills/foodshare-architecture/SKILL.md

# Add a workflow
code .claude/skills/foodshare-architecture/workflows/new-workflow.md
```

## 📊 Coverage

| Domain | Covered |
|--------|---------|
| Architecture | ✅ Clean Architecture, MVVM |
| UI | ✅ Liquid Glass, accessibility |
| Backend | ✅ Supabase, RLS, Edge Functions |
| Testing | ✅ Swift Testing, mocks |
| Concurrency | ✅ Swift 6, Sendable |
| Performance | ✅ ProMotion, GPU |
| Offline | ✅ Core Data, sync |
| Deployment | ✅ Xcode Cloud, TestFlight |
| Review | ✅ PR reviews, audits |
| Feature | ✅ End-to-end workflow |

## 💡 Pro Tips

1. **Skills compose**: Multiple skills work together seamlessly
2. **Skills learn**: Update them as Foodshare evolves
3. **Skills teach**: New team members learn by using them
4. **Skills enforce**: Consistent patterns across codebase

## 🔗 Related Documentation

- [CLAUDE.md](/CLAUDE.md) - Project overview and rules
- [docs/ARCHITECTURE.md](/docs/ARCHITECTURE.md) - Architecture deep-dive
- [supabase/README.md](/supabase/README.md) - Backend setup

---

**"Skills are codified expertise. They help you ship excellent code faster."** 🚀
