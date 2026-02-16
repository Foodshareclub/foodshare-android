#!/bin/bash

# lib/logging.sh
# Centralized logging utilities for Foodshare Xcode Cloud CI scripts
#
# Provides structured logging with severity levels, timestamps, and colors

# Log levels
LOG_LEVEL_DEBUG=0
LOG_LEVEL_INFO=1
LOG_LEVEL_WARN=2
LOG_LEVEL_ERROR=3
LOG_LEVEL_CRITICAL=4

# Current log level (set via LOG_LEVEL env var, default INFO)
CURRENT_LOG_LEVEL=${LOG_LEVEL:-$LOG_LEVEL_INFO}

# Log file (set via LOG_FILE env var)
LOG_FILE="${LOG_FILE:-}"

# Colors (disabled in CI by default, can be enabled via FORCE_COLOR=1)
if [ "${FORCE_COLOR:-0}" = "1" ] || [ -t 1 ]; then
    COLOR_RESET='\033[0m'
    COLOR_RED='\033[0;31m'
    COLOR_YELLOW='\033[0;33m'
    COLOR_GREEN='\033[0;32m'
    COLOR_BLUE='\033[0;34m'
    COLOR_CYAN='\033[0;36m'
    COLOR_GRAY='\033[0;90m'
else
    COLOR_RESET=''
    COLOR_RED=''
    COLOR_YELLOW=''
    COLOR_GREEN=''
    COLOR_BLUE=''
    COLOR_CYAN=''
    COLOR_GRAY=''
fi

# Get timestamp
get_timestamp() {
    date '+%Y-%m-%d %H:%M:%S'
}

# Log message with level
log_message() {
    local level=$1
    local emoji=$2
    local color=$3
    local tag=$4
    shift 4
    local message="$*"

    # Check if we should log this level
    if [ "$level" -lt "$CURRENT_LOG_LEVEL" ]; then
        return
    fi

    local timestamp
    timestamp=$(get_timestamp)
    local formatted="${color}${emoji} [${tag}]${COLOR_RESET} $message"

    # Print to console
    echo -e "$formatted"

    # Write to log file if specified
    if [ -n "$LOG_FILE" ]; then
        echo "[$timestamp] [$tag] $message" >> "$LOG_FILE"
    fi
}

# Debug log
log_debug() {
    log_message $LOG_LEVEL_DEBUG "🔍" "$COLOR_GRAY" "DEBUG" "$@"
}

# Info log
log_info() {
    log_message $LOG_LEVEL_INFO "ℹ️" "$COLOR_BLUE" "INFO" "$@"
}

# Success log
log_success() {
    log_message $LOG_LEVEL_INFO "✅" "$COLOR_GREEN" "SUCCESS" "$@"
}

# Warning log
log_warn() {
    log_message $LOG_LEVEL_WARN "⚠️" "$COLOR_YELLOW" "WARNING" "$@"
}

# Error log
log_error() {
    log_message $LOG_LEVEL_ERROR "❌" "$COLOR_RED" "ERROR" "$@"
}

# Critical error log
log_critical() {
    log_message $LOG_LEVEL_CRITICAL "🚨" "$COLOR_RED" "CRITICAL" "$@"
}

# Phase header
log_phase() {
    local phase_name="$1"
    echo ""
    echo "================================================================"
    log_info "PHASE: $phase_name"
    echo "================================================================"
    echo ""
}

# Section header
log_section() {
    local section_name="$1"
    echo ""
    log_info "━━━ $section_name ━━━"
    echo ""
}

# Diagnostic log
log_diagnostic() {
    log_message $LOG_LEVEL_DEBUG "🔬" "$COLOR_CYAN" "DIAGNOSTIC" "$@"
}

# Performance metric
log_metric() {
    local metric_name="$1"
    local metric_value="$2"
    log_message $LOG_LEVEL_INFO "📊" "$COLOR_CYAN" "METRIC" "$metric_name: $metric_value"
}

# Step log (for progress tracking)
log_step() {
    local step_num="$1"
    local total_steps="$2"
    local step_desc="$3"
    log_info "Step $step_num/$total_steps: $step_desc"
}

# Export functions
export -f get_timestamp
export -f log_message
export -f log_debug
export -f log_info
export -f log_success
export -f log_warn
export -f log_error
export -f log_critical
export -f log_phase
export -f log_section
export -f log_diagnostic
export -f log_metric
export -f log_step
