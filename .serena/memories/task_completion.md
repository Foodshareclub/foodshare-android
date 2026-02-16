# Task Completion Checklist

## After Making Changes
1. **Format**: `foodshare-hooks format` or `foodshare-hooks fix --staged`
2. **Lint**: `foodshare-hooks lint` (check for style violations)
3. **Test**: `foodshare-hooks test` (run unit tests)
4. **Build**: `foodshare-hooks build` (verify compilation)

## Before Committing
1. Stage relevant files
2. Run `foodshare-hooks fix --staged` (format + lint staged files)
3. Run `foodshare-hooks test` (ensure tests pass)
4. Commit with conventional message: `type(scope): description`

## Commit Message Format
Types: feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert
Examples:
- `feat(feed): add category filtering`
- `fix(auth): resolve token refresh issue`
- `refactor(design): consolidate glass components`

## Before Pushing
1. Ensure all local commits are clean
2. Pre-push hook runs: validate, memory, deps-graph, version checks
3. In full mode: also runs test and build

## Code Quality Gates
- SwiftFormat: 120 char line length, Swift 6.2
- SwiftLint: Configured rules in `.swiftlint.yml`
- Test coverage: 70%+ target
- No hardcoded secrets
