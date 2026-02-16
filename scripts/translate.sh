#!/bin/bash
# chmod +x scripts/translate.sh

# Enterprise Translation Management CLI v2.0
set -e

BASE_URL="https://api.foodshare.club/functions/v1"
BFF_URL="${BASE_URL}/bff"
SUPPORTED_LOCALES="cs de es fr pt ru uk zh hi ar it pl nl ja ko tr vi id th sv"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; CYAN='\033[0;36m'; NC='\033[0m'

print_header() { echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n${BLUE}  $1${NC}\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"; }

check_health() {
    print_header "Endpoint Health Check"
    echo -e "${YELLOW}Checking endpoints...${NC}\n"

    bff_status=$(curl -s -o /dev/null -w "%{http_code}" "${BFF_URL}" 2>/dev/null)
    [ "$bff_status" = "200" ] && echo -e "  BFF:              ${GREEN}OK${NC}" || echo -e "  BFF:              ${RED}Error${NC}"

    bff_trans=$(curl -s -o /dev/null -w "%{http_code}" "${BFF_URL}/translations?locale=en" 2>/dev/null)
    [ "$bff_trans" = "200" ] && echo -e "  BFF/translations: ${GREEN}OK${NC}" || echo -e "  BFF/translations: ${RED}Error${NC}"

    gt_health=$(curl -s "${BASE_URL}/get-translations/health" 2>/dev/null)
    gt_status=$(echo "$gt_health" | jq -r '.status // "error"')
    [ "$gt_status" = "ok" ] && echo -e "  get-translations: ${GREEN}OK${NC} (v$(echo "$gt_health" | jq -r '.version'))" || echo -e "  get-translations: ${RED}Error${NC}"

    audit_status=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/translation-audit" 2>/dev/null)
    [ "$audit_status" = "200" ] && echo -e "  translation-audit:${GREEN}OK${NC}" || echo -e "  translation-audit:${RED}Error${NC}"
    echo ""
}

audit_locale() {
    local locale=$1
    result=$(curl -s "${BASE_URL}/translation-audit?locale=${locale}&limit=10")
    total=$(echo "$result" | jq -r '.totalKeys // 0')
    untranslated=$(echo "$result" | jq -r '.untranslatedCount // 0')
    if [ "$total" -gt 0 ]; then
        coverage=$(echo "scale=1; (($total - $untranslated) * 100) / $total" | bc)
        [ $(echo "$coverage >= 90" | bc -l) -eq 1 ] && color=$GREEN || { [ $(echo "$coverage >= 70" | bc -l) -eq 1 ] && color=$YELLOW || color=$RED; }
        printf "  %-5s: ${color}%5.1f%%${NC} (%d/%d)\n" "$locale" "$coverage" "$((total - untranslated))" "$total"
    fi
}

audit_all() {
    print_header "Translation Coverage Audit"
    for locale in $SUPPORTED_LOCALES; do audit_locale "$locale"; done
    echo ""
}

show_status() {
    print_header "Translation System Status"
    health=$(curl -s "${BASE_URL}/get-translations/health")
    status=$(echo "$health" | jq -r '.status // "unknown"')
    [ "$status" = "ok" ] && echo -e "  Service: ${GREEN}Healthy${NC} (v$(echo "$health" | jq -r '.version'))" || echo -e "  Service: ${RED}Unhealthy${NC}"

    summary=$(curl -s "${BASE_URL}/translation-audit?all=true")
    total_locales=$(echo "$summary" | jq -r '.localeCount // 0')
    total_untranslated=$(echo "$summary" | jq -r '.totalUntranslated // 0')
    en_keys=$(echo "$summary" | jq -r '.englishKeyCount // 0')

    echo -e "  Locales: ${total_locales} | Keys: ${en_keys} | Untranslated: ${YELLOW}${total_untranslated}${NC}"
    total_possible=$((en_keys * total_locales))
    [ "$total_possible" -gt 0 ] && echo -e "  Coverage: ${GREEN}$(echo "scale=1; (($total_possible - $total_untranslated) * 100) / $total_possible" | bc)%${NC}"

    echo -e "\n${YELLOW}Top 5 needing work:${NC}"
    echo "$summary" | jq -r '.locales | .[0:5] | .[] | "  \(.locale): \(.untranslatedCount) missing"'
}

case "${1:-status}" in
    health) check_health ;;
    audit) [ -n "$2" ] && audit_locale "$2" || audit_all ;;
    status) show_status ;;
    *) echo -e "${CYAN}Enterprise Translation CLI v2.0${NC}\nUsage: $0 {status|health|audit [locale]}" ;;
esac
