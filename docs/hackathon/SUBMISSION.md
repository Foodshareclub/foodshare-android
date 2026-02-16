# Hackathon Submission — Built with Opus 4.6

> Cerebral Valley x Anthropic | Feb 10-16, 2026 | Deadline: Feb 16, 3:00 PM EST

---

## Form Fields

### Team Name
Foodshare

### Team Members
Tarlan Isaev (@organicnz)

### Project Name
FoodShare — Cross-Platform Food Sharing App

### Selected Hackathon Track
Break the Barriers

---

### Project Description (997 chars)

Every year, $1 trillion worth of food is thrown away while 783 million people go hungry. One billion meals wasted daily. It's not a supply problem — it's a distribution problem.

FoodShare connects people with surplus food to those who need it. Simple, local, direct.

Built from scratch with Claude Code (Opus 4.6) by a solo developer in one week. Single Swift codebase → native iOS + Android via Skip Fuse:

• 584 Swift files (~198K LOC), 24 feature modules — messaging, map discovery, forums, challenges, reviews, donations
• 137-component "Liquid Glass" design system with 8 themes
• 29 core modules — offline sync, caching, analytics, i18n (21 languages)
• 35 self-hosted Supabase Edge Functions — auth, search, AI, geocoding, notifications

Claude Code designed the architecture, scaffolded every feature, built the networking and design layers, and maintained consistent patterns across 584 files without drift. What would take a team 3-6 months — one person, one week.

---

### Public GitHub Repository

```
https://github.com/Foodshareclub/foodshare-app
```

---

### Demo Video

```
TODO: Record 3-minute demo and paste link here
```

---

### Thoughts and feedback on building with Opus 4.6 (992 chars)

Opus 4.6 via Claude Code wasn't autocomplete — it was a senior engineering partner.

Architecture at scale: Designed the full system (Clean Architecture + MVVM), then maintained patterns across 584 files and 24 features without drift.

Cross-platform reasoning: Skip Fuse compiles Swift to iOS + Android, but not every API works on both. Opus learned fast — guarding iOS-only code (Metal, UIKit, Keychain) with #if !SKIP while finding alternatives.

198K-line awareness: Referenced design tokens, reused utilities, wired features into the router without prompting. Never created duplicates.

Full-stack: Built 35 Edge Functions, networking with retry + Supabase fallbacks, and a 137-component design system with 120Hz animations.

Where it needed me: Skip Fuse transpiler edge cases. Everything else — architecture, features, infra, backend, design — handled with minimal correction.

Shipped in one week what would take a team 3-6 months. Opus 4.6 changes what one developer can do.

---

## Submission Checklist

- [x] Team name: Foodshare
- [x] Team members: Tarlan Isaev (@organicnz)
- [x] Project name: FoodShare — Cross-Platform Food Sharing App
- [x] Track: Break the Barriers
- [x] Project description (under 1000 chars)
- [x] GitHub repository (public)
- [x] Opus 4.6 feedback (under 1000 chars)
- [ ] Demo video (3-min, YouTube/Loom)
