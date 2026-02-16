#!/bin/bash
# Configure the before-user-created auth hook for api-v1-geocoding edge function
#
# Self-hosted Supabase: Auth hooks are configured via environment variables
# in docker-compose.yml and .env on the VPS.
#
# NOTE: GoTrue rejects http:// hook URIs for non-localhost hosts.
# For self-hosted, use either:
#   - pg:// database hooks (recommended)
#   - https:// via the public API URL
#
# To enable this hook on the VPS:
# 1. SSH into VPS: ssh -i ~/.ssh/id_rsa_gitlab organic@152.53.136.84
# 2. Edit /home/organic/dev/foodshare-backend/.env and uncomment:
#      HOOK_BEFORE_USER_CREATED_ENABLED=true
#      HOOK_BEFORE_USER_CREATED_URI=https://api.foodshare.club/functions/v1/api-v1-geocoding
#      HOOK_BEFORE_USER_CREATED_SECRETS=v1,whsec_H1FKF4nExp13Axg51dUZ7j1G8xAc14/xoQtPiv6Jsm0=
# 3. Uncomment the corresponding GOTRUE_HOOK_* lines in docker-compose.yml
# 4. Restart auth: docker compose restart auth
# 5. Verify: curl -s https://api.foodshare.club/auth/v1/settings | jq '.hook_before_user_created'

HOOK_URL="https://api.foodshare.club/functions/v1/api-v1-geocoding"

echo "=== Self-Hosted Auth Hook Configuration ==="
echo ""
echo "Hook URL: $HOOK_URL"
echo ""

# Check current hook status via GoTrue settings endpoint
echo "Checking current hook status..."
SETTINGS=$(curl -s "https://api.foodshare.club/auth/v1/settings" 2>/dev/null)

if [ $? -eq 0 ] && [ -n "$SETTINGS" ]; then
    echo "Current auth settings (hook-related):"
    echo "$SETTINGS" | grep -i "hook" || echo "No hook fields found in settings"
else
    echo "Could not reach auth settings endpoint"
fi

echo ""
echo "To configure the hook, update the VPS docker-compose environment."
echo "See comments at the top of this script for instructions."
echo ""
echo "=== Done ==="
