#!/usr/bin/env bash
# Prints ns/op for a 32-struct document.
# Pass "long" for a JIT-steady sample (warmup 4000, 20000 iterations).
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
if [[ "${1:-}" == "long" ]]; then
  ./gradlew -q microbench --no-daemon --args=long
else
  ./gradlew -q microbench --no-daemon
fi
