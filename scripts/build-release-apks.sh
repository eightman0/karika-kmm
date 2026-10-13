#!/usr/bin/env bash
# Builds signed release APKs for launcher and/or salesrep using each module's own
# keystore.properties (see launcher/keystore.properties, salesrep/keystore.properties) - the same
# config Gradle already picks up automatically, just invoked headlessly instead of through Android
# Studio's "Generate Signed Bundle/APK" wizard.
#
# Publishing is manual: upload the APK from dist/ in Karika Ops > Verzije (opened from Magento
# admin > Karika Ops > Tableti) on the matching tab. The old dashboard's /versions/publish is gone.
#
# The launcher is Device Owner, provisioned by factory-reset+QR-scan, and can also self-update
# remotely ("Ažuriraj launcher" in Karika Ops). Self-update only installs a strictly higher
# versionCode, so bump build.gradle.kts's versionCode for the launcher on every real change.
#
# shop is composeApp's kiosk flavor (karika.distribucija.ba.kiosk), published under app=shop -
# signed with composeApp/keystore.properties.
#
# salesrep and shop are built for one backend environment, the second argument (prod by default),
# passed to Gradle as -PkarikaEnv. The package is the same for every environment: each Karika Ops
# (stage, demo, prod) publishes its own build, and a tablet takes it from the Karika Ops its QR's
# api_base_url points to. The launcher has no environment of its own.
#
# Usage: scripts/build-release-apks.sh [launcher|salesrep|shop|all] [prod|stage|demo]
# Defaults to "all" and "prod". Output APKs are copied to dist/ at the repo root.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

DIST_DIR="$REPO_ROOT/dist"
TARGET="all"
APP_ENV="prod"

for arg in "$@"; do
  case "$arg" in
    launcher|salesrep|shop|all) TARGET="$arg" ;;
    prod|stage|demo) APP_ENV="$arg" ;;
    *)
      echo "Usage: $0 [launcher|salesrep|shop|all] [prod|stage|demo]" >&2
      exit 1
      ;;
  esac
done

MODULES=()
if [ "$TARGET" = "all" ]; then
  MODULES=(launcher salesrep shop)
else
  MODULES=("$TARGET")
fi

# Gradle module, assemble task and output APK per target - shop is a flavor of composeApp, the
# others are their own modules.
gradle_module() { [ "$1" = "shop" ] && echo composeApp || echo "$1"; }
assemble_task() { [ "$1" = "shop" ] && echo ":composeApp:assembleKioskRelease" || echo ":$1:assembleRelease"; }
output_apk() {
  if [ "$1" = "shop" ]; then
    echo "$REPO_ROOT/composeApp/build/outputs/apk/kiosk/release/composeApp-kiosk-release.apk"
  else
    echo "$REPO_ROOT/$1/build/outputs/apk/release/$1-release.apk"
  fi
}

for target in "${MODULES[@]}"; do
  module="$(gradle_module "$target")"
  if [ ! -f "$REPO_ROOT/$module/keystore.properties" ]; then
    echo "Error: $module/keystore.properties not found - release signingConfig won't be applied," >&2
    echo "and the release build type would silently come out unsigned. Put the real keystore" >&2
    echo "path/passwords there first (see .gitignore - it's deliberately never committed)." >&2
    exit 1
  fi
done

GRADLE_TASKS=()
for module in "${MODULES[@]}"; do
  GRADLE_TASKS+=("$(assemble_task "$module")")
done

# --rerun-tasks forces every task to actually execute instead of being skipped as UP-TO-DATE;
# --no-build-cache/--no-configuration-cache stop it reusing outputs from a previous invocation
# (both are on by default project-wide, see gradle.properties) - a release build should never
# ship something that wasn't compiled/packaged/signed fresh, this run, from what's on disk now.
GRADLE_NO_CACHE_FLAGS=(--rerun-tasks --no-build-cache --no-configuration-cache)

echo "Running: ./gradlew ${GRADLE_TASKS[*]} -PkarikaEnv=$APP_ENV ${GRADLE_NO_CACHE_FLAGS[*]}"
./gradlew "${GRADLE_TASKS[@]}" -PkarikaEnv="$APP_ENV" "${GRADLE_NO_CACHE_FLAGS[@]}" --console=plain

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
  apk="$(output_apk "$module")"
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

  gradle_file="$REPO_ROOT/$(gradle_module "$module")/build.gradle.kts"
  version_name="$(grep -m1 'versionName' "$gradle_file" | sed -E 's/.*versionName = "([^"]*)".*/\1/')"
  version_code="$(grep -m1 'versionCode' "$gradle_file" | sed -E 's/[^0-9]*([0-9]+).*/\1/')"
  # The environment is in the name, since every environment's APK has the same package
  name="$module"
  [ "$module" != "launcher" ] && name="$module-$APP_ENV"
  dest="$DIST_DIR/${name}-release-v${version_code}-${version_name}.apk"
  cp "$apk" "$dest"

  echo "[$module] sha256: $(shasum -a 256 "$dest" | awk '{print $1}')"
  echo "[$module] -> $dest"
done

echo "Done."
