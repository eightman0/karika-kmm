#!/usr/bin/env bash
# Builds signed release APKs for launcher and/or salesrep using each module's own
# keystore.properties (see launcher/keystore.properties, salesrep/keystore.properties) - the same
# config Gradle already picks up automatically, just invoked headlessly instead of through Android
# Studio's "Generate Signed Bundle/APK" wizard.
#
# Usage: scripts/build-release-apks.sh [launcher|salesrep|all]
# Defaults to "all". Output APKs are copied to dist/ at the repo root.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

DIST_DIR="$REPO_ROOT/dist"
TARGET="${1:-all}"

case "$TARGET" in
  launcher|salesrep|all) ;;
  *)
    echo "Usage: $0 [launcher|salesrep|all]" >&2
    exit 1
    ;;
esac

MODULES=()
if [ "$TARGET" = "all" ]; then
  MODULES=(launcher salesrep)
else
  MODULES=("$TARGET")
fi

for module in "${MODULES[@]}"; do
  if [ ! -f "$REPO_ROOT/$module/keystore.properties" ]; then
    echo "Error: $module/keystore.properties not found - release signingConfig won't be applied," >&2
    echo "and the release build type would silently come out unsigned. Put the real keystore" >&2
    echo "path/passwords there first (see .gitignore - it's deliberately never committed)." >&2
    exit 1
  fi
done

GRADLE_TASKS=()
for module in "${MODULES[@]}"; do
  GRADLE_TASKS+=(":${module}:assembleRelease")
done

echo "Running: ./gradlew ${GRADLE_TASKS[*]}"
./gradlew "${GRADLE_TASKS[@]}" --console=plain

# Locate apksigner (from the SDK local.properties already points Gradle at) to confirm each APK
# actually got signed, rather than trusting the build succeeded silently with no signingConfig.
ANDROID_SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$ANDROID_SDK" ] && [ -f "$REPO_ROOT/local.properties" ]; then
  ANDROID_SDK="$(sed -n 's/^sdk\.dir=//p' "$REPO_ROOT/local.properties")"
fi
APKSIGNER=""
if [ -n "$ANDROID_SDK" ]; then
  APKSIGNER="$(find "$ANDROID_SDK/build-tools" -maxdepth 1 -name '3*' 2>/dev/null | sort -V | tail -1)/apksigner"
  [ -x "$APKSIGNER" ] || APKSIGNER=""
fi

mkdir -p "$DIST_DIR"

for module in "${MODULES[@]}"; do
  apk="$REPO_ROOT/$module/build/outputs/apk/release/${module}-release.apk"
  if [ ! -f "$apk" ]; then
    echo "Error: expected output APK not found at $apk" >&2
    exit 1
  fi

  if [ -n "$APKSIGNER" ]; then
    if ! "$APKSIGNER" verify "$apk" >/dev/null 2>&1; then
      echo "Error: $apk did not pass apksigner verify - not properly signed." >&2
      exit 1
    fi
    echo "[$module] signature OK"
  else
    echo "[$module] warning: apksigner not found, skipping signature verification" >&2
  fi

  if [ "$module" = "launcher" ]; then
    # Launcher is Device Owner and isn't versioned/published through the dashboard the way
    # salesrep is - a version suffix here would just be noise.
    dest="$DIST_DIR/${module}-release.apk"
  else
    version_name="$(grep -m1 'versionName' "$REPO_ROOT/$module/build.gradle.kts" | sed -E 's/.*versionName = "([^"]*)".*/\1/')"
    version_code="$(grep -m1 'versionCode' "$REPO_ROOT/$module/build.gradle.kts" | sed -E 's/[^0-9]*([0-9]+).*/\1/')"
    dest="$DIST_DIR/${module}-release-v${version_code}-${version_name}.apk"
  fi
  cp "$apk" "$dest"

  echo "[$module] sha256: $(shasum -a 256 "$dest" | awk '{print $1}')"
  echo "[$module] -> $dest"
done

echo "Done."
