#!/usr/bin/env bash
# Tests for scripts/check-release-version.sh — run anywhere:
#   bash scripts/check-release-version.test.sh
set -uo pipefail

here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
script="$here/check-release-version.sh"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT

cat > "$tmp/build.gradle.kts" <<'EOF'
    defaultConfig {
        applicationId = "com.mgneg.fakecall"
        versionCode = 4
        versionName = "1.3"
    }
EOF

pass=0
fail=0
expect() { # <want-exit> <description> <args...>
  local want="$1" desc="$2"; shift 2
  bash "$script" "$@" > /dev/null 2>&1
  local got=$?
  if [ "$got" -eq "$want" ]; then
    echo "ok   - $desc"
    pass=$((pass + 1))
  else
    echo "FAIL - $desc (wanted exit $want, got $got)"
    fail=$((fail + 1))
  fi
}

expect 0 "matching tag and higher code passes" v1.3 "$tmp/build.gradle.kts" 3
expect 0 "first release (no previous code) passes" v1.3 "$tmp/build.gradle.kts" 0
expect 1 "wrong tag is refused" v1.4 "$tmp/build.gradle.kts" 3
expect 1 "tag without v prefix is refused" 1.3 "$tmp/build.gradle.kts" 3
expect 1 "equal versionCode is refused" v1.3 "$tmp/build.gradle.kts" 4
expect 1 "lower versionCode is refused" v1.3 "$tmp/build.gradle.kts" 5
expect 1 "missing gradle file is refused" v1.3 "$tmp/does-not-exist.kts" 3

echo "---"
echo "$pass passed, $fail failed"
[ "$fail" -eq 0 ]
