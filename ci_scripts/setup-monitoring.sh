#!/bin/bash
# chmod +x ci_scripts/setup-monitoring.sh

# Production Monitoring Setup Guide
# This script helps set up monitoring for the self-hosted infrastructure

cat << 'EOF'
===========================================
FoodShare Production Monitoring Setup
===========================================

1. API Health Monitoring (api.foodshare.club)
   - Set up uptime monitoring (e.g., UptimeRobot, Pingdom)
   - Monitor endpoint: https://api.foodshare.club/health
   - Alert on: 5xx errors, response time > 2s, downtime > 1min

2. Database Monitoring (Supabase)
   - Monitor connection pool usage
   - Track query performance
   - Set alerts for slow queries (> 1s)

3. App Analytics
   - Track key metrics:
     * Daily Active Users (DAU)
     * Food items shared/claimed
     * User retention rate
     * Crash-free sessions

4. Error Tracking
   - Set up Sentry or similar for crash reporting
   - Monitor error rates by version
   - Track API error responses

5. Performance Metrics
   - App launch time
   - Screen load times
   - Network request latency
   - Memory usage

Recommended Tools:
- Uptime: UptimeRobot (free tier)
- Analytics: Firebase Analytics (free)
- Crashes: Sentry (free tier)
- APM: New Relic or Datadog

Next Steps:
1. Choose monitoring tools
2. Add SDK integrations to app
3. Set up alerting rules
4. Create monitoring dashboard

EOF
