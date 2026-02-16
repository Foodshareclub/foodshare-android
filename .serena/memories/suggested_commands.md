# Foodshare CLI Commands

## Primary Development
```bash
foodshare-hooks run --logs     # Build + install + launch + stream logs
foodshare-hooks test           # Run unit tests
foodshare-hooks fix --staged   # Format + lint staged files
foodshare-hooks doctor         # Environment diagnostics
```

## Build Commands
```bash
foodshare-hooks build           # Build debug
foodshare-hooks build release   # Build release
foodshare-hooks build --clean   # Clean build
foodshare-hooks workaround      # Build with dep file fix
```

## Testing
```bash
foodshare-hooks test unit       # Unit tests only
foodshare-hooks test all        # All tests
foodshare-hooks test --coverage # With coverage report
```

## Code Quality
```bash
foodshare-hooks format          # Run SwiftFormat
foodshare-hooks lint            # Run SwiftLint
foodshare-hooks fix             # Format + lint combined
foodshare-hooks analyze         # Code metrics (LOC, TODOs)
foodshare-hooks secrets         # Scan for hardcoded secrets
```

## Simulator
```bash
foodshare-hooks simulator list      # List available simulators
foodshare-hooks simulator boot      # Boot default simulator
foodshare-hooks logs               # Stream app logs
```

## Supabase
```bash
npx supabase start              # Start local Supabase
npx supabase db push            # Push migrations
npx supabase functions deploy   # Deploy edge functions
```

## System Commands (Darwin)
- `git`, `ls`, `cd`, `grep`, `find` work as expected
- Use `open` to open files/directories in Finder
- Use `pbcopy`/`pbpaste` for clipboard
