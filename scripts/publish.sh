#!/usr/bin/env bash
# Publish io.github.leo-gan:gld-ion-kotlin to Maven Central.
# Register the namespace and the signing key once. See docs/publishing.md.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
cd "${root}"

dry_run=0
if [[ "${1:-}" == "--dry-run" ]]; then
  dry_run=1
elif [[ -n "${1:-}" ]]; then
  echo "usage: scripts/publish.sh [--dry-run]" >&2
  exit 2
fi

version="$(sed -n 's/^version=//p' gradle.properties | head -n 1)"
if [[ -z "${version}" ]]; then
  echo "gradle.properties has no version" >&2
  exit 1
fi

if [[ "${GITHUB_REF_TYPE:-}" == "tag" ]]; then
  tag_name="${GITHUB_REF_NAME}"
  tag_version="${tag_name#v}"
  if [[ "${tag_version}" != "${version}" ]]; then
    echo "git tag ${tag_name} does not match gradle.properties version ${version}" >&2
    exit 1
  fi
else
  tag_name="${version}"
fi

block="$(cat <<EOF
Maven Central coordinate \`io.github.leo-gan:gld-ion-kotlin:${version}\`.

\`\`\`kotlin
dependencies {
    implementation("io.github.leo-gan:gld-ion-kotlin:${version}")
}
\`\`\`
EOF
)"
printf '%s\n' "${block}"

if [[ "${dry_run}" -eq 1 ]]; then
  echo "dry-run: Maven Central was not called"
  exit 0
fi

if [[ -z "${MAVEN_CENTRAL_USERNAME:-}" || -z "${MAVEN_CENTRAL_PASSWORD:-}" ]]; then
  echo "MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD are required. See docs/publishing.md" >&2
  exit 1
fi
if [[ -z "${SIGNING_KEY:-}${SIGNING_KEY_BASE64:-}" ]]; then
  echo "SIGNING_KEY or SIGNING_KEY_BASE64 is required. See docs/publishing.md" >&2
  exit 1
fi

key="${SIGNING_KEY:-}"
if [[ -z "${key}" ]]; then
  key="$(printf '%s' "${SIGNING_KEY_BASE64}" | base64 -d)"
fi
if [[ "${key}" != *$'\n'* && "${key}" == *'\\n'* ]]; then
  key="${key//\\n/$'\n'}"
fi
if [[ "${key}" != *"BEGIN PGP PRIVATE KEY BLOCK"* ]]; then
  echo "SIGNING_KEY is not an armored PGP private key" >&2
  exit 1
fi

export ORG_GRADLE_PROJECT_mavenCentralUsername="${MAVEN_CENTRAL_USERNAME}"
export ORG_GRADLE_PROJECT_mavenCentralPassword="${MAVEN_CENTRAL_PASSWORD}"
export ORG_GRADLE_PROJECT_signingInMemoryKey="${key}"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="${SIGNING_PASSWORD:-}"

if [[ "${GITHUB_ACTIONS:-}" == "true" && "${GITHUB_REF_TYPE:-}" == "tag" ]]; then
  printf '%s\n' "${block}" | python3 scripts/github_release.py notes --tag "${tag_name}"
fi

./gradlew publishAndReleaseToMavenCentral --no-daemon
