#!/usr/bin/env bash
# Verify a release tag against app/build.gradle.kts:
#   - the tag must be exactly "v<versionName>"
#   - versionCode must be strictly greater than the previous release's
#
# Usage: check-release-version.sh <tag> <build.gradle.kts> [previous-versionCode]
# Exits non-zero (and prints an ::error:: line) on any mismatch.
set -euo pipefail

tag="${1:?usage: check-release-version.sh <tag> <gradle-file> [previous-versionCode]}"
gradle_file="${2:?usage: check-release-version.sh <tag> <gradle-file> [previous-versionCode]}"
prev_code="${3:-0}"

if [ ! -f "$gradle_file" ]; then
  echo "::error::build file not found: $gradle_file"
  exit 1
fi

version_name="$(grep -E 'versionName[[:space:]]*=' "$gradle_file" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
version_code="$(grep -E 'versionCode[[:space:]]*=' "$gradle_file" | head -1 | sed -E 's/[^0-9]*([0-9]+).*/\1/')"

if [ -z "$version_name" ] || [ -z "$version_code" ]; then
  echo "::error::could not parse versionName/versionCode from $gradle_file"
  exit 1
fi

if [ "$tag" != "v${version_name}" ]; then
  echo "::error::tag $tag does not match versionName $version_name (expected v${version_name})"
  exit 1
fi

if [ "$version_code" -le "$prev_code" ]; then
  echo "::error::versionCode $version_code is not greater than the previous release's $prev_code"
  exit 1
fi

echo "OK: tag $tag matches versionName $version_name; versionCode $version_code > $prev_code"
