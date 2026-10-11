#!/usr/bin/env bash
# Regenerate into a temp directory and diff against the committed sources.
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
./gradlew -q installDist --no-daemon
cli="$root/build/install/gld-iongen-kotlin/bin/gld-iongen-kotlin"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
shopt -s nullglob
for schema in testdata/schema/*.json testdata/schema/*.ion; do
  "$cli" --schema "$schema" --out "$tmp"
done
diff -ru "$root/src/test/kotlin/ion/generated" "$tmp"
