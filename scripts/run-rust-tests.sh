#!/bin/bash
cd "$(dirname "$0")/../tools"
cargo test -p foodshare-hooks-tests --all-features
