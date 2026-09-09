#!/usr/bin/env bash
# Builds signed release APKs for launcher and/or salesrep using each module's own
# keystore.properties (see launcher/keystore.properties, salesrep/keystore.properties) - the same
# config Gradle already picks up automatically, just invoked headlessly instead of through Android
# Studio's "Generate Signed Bundle/APK" wizard.
#
# Building either module also stages the APK on the admin dashboard (POST /versions/publish, with
# an app=launcher|salesrep form field) unless --no-publish is given - same as uploading it by hand
# on the /versions page's matching tab, just scripted. This only stages it (see version_config.py/
# launcher_version_config.py) - no device installs anything until it's explicitly sent to chosen
# devices or to everyone, so it's safe to run on every build. Needs KARIKA_DASHBOARD_URL/
# KARIKA_ADMIN_USERNAME/KARIKA_ADMIN_PASSWORD - either exported already or defined in
# scripts/publish.env (gitignored, see scripts/publish.env.example).
#
# The launcher is still Device Owner, provisioned by factory-reset+QR-scan same as always - but it
# can now ALSO self-update remotely (see the dashboard's "Ažuriraj launcher" action), which is what
# actually reads whatever gets published here. Reusing a versionCode across two different launcher
# publishes would overwrite the older one's Storage blob at launcher-releases/{versionCode}.apk -
# the exact corruption class hit twice already for salesrep - so bump build.gradle.kts's
# versionCode for the launcher on every real change, same discipline as salesrep already has.
#
# Usage: scripts/build-release-apks.sh [launcher|salesrep|all] [--no-publish]
# Defaults to "all". Output APKs are copied to dist/ at the repo root.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

DIST_DIR="$REPO_ROOT/dist"
TARGET="all"
DO_PUBLISH=1

for arg in "$@"; do
  case "$arg" in
    launcher|salesrep|all) TARGET="$arg" ;;
    --no-publish) DO_PUBLISH=0 ;;
    *)
      echo "Usage: $0 [launcher|salesrep|all] [--no-publish]" >&2
      exit 1
      ;;
  esac
done

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

# --rerun-tasks forces every task to actually execute instead of being skipped as UP-TO-DATE;
# --no-build-cache/--no-configuration-cache stop it reusing outputs from a previous invocation
# (both are on by default project-wide, see gradle.properties) - a release build should never
# ship something that wasn't compiled/packaged/signed fresh, this run, from what's on disk now.
GRADLE_NO_CACHE_FLAGS=(--rerun-tasks --no-build-cache --no-configuration-cache)

echo "Running: ./gradlew ${GRADLE_TASKS[*]} ${GRADLE_NO_CACHE_FLAGS[*]}"
./gradlew "${GRADLE_TASKS[@]}" "${GRADLE_NO_CACHE_FLAGS[@]}" --console=plain

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

publish_apk() {
  local module="$1"
  local apk="$2"

  if [ -f "$REPO_ROOT/scripts/publish.env" ]; then
    # shellcheck disable=SC1091
    source "$REPO_ROOT/scripts/publish.env"
  fi
  local url="${KARIKA_DASHBOARD_URL:-}"
  local user="${KARIKA_ADMIN_USERNAME:-}"
  local pass="${KARIKA_ADMIN_PASSWORD:-}"
  if [ -z "$url" ] || [ -z "$user" ] || [ -z "$pass" ]; then
    echo "[$module] skipping dashboard publish: KARIKA_DASHBOARD_URL/KARIKA_ADMIN_USERNAME/KARIKA_ADMIN_PASSWORD" >&2
    echo "[$module] not set - export them, or copy scripts/publish.env.example to scripts/publish.env" >&2
    return 0
  fi

  local cookie_jar
  cookie_jar="$(mktemp)"
  # shellcheck disable=SC2064
  trap "rm -f '$cookie_jar'" RETURN

  local login_redirect
  login_redirect="$(curl -fsS -o /dev/null -w '%{redirect_url}' -c "$cookie_jar" \
    --data-urlencode "username=$user" --data-urlencode "password=$pass" \
    "$url/login")"
  case "$login_redirect" in
    */devices) ;;
    *)
      echo "[$module] dashboard login failed (redirected to: $login_redirect) - check credentials" >&2
      return 1
      ;;
  esac

  local publish_redirect
  publish_redirect="$(curl -fsS -o /dev/null -w '%{redirect_url}' -b "$cookie_jar" \
    -F "apk_file=@${apk};type=application/vnd.android.package-archive" \
    -F "app=${module}" \
    "$url/versions/publish")"
  case "$publish_redirect" in
    *error=*)
      echo "[$module] dashboard publish failed: $publish_redirect" >&2
      return 1
      ;;
  esac
  echo "[$module] staged on dashboard as the pending version - use the devices list or"
  echo "[$module] \"Posalji svima\" on $url/versions?app=$module to actually send it to devices"
}

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

  version_name="$(grep -m1 'versionName' "$REPO_ROOT/$module/build.gradle.kts" | sed -E 's/.*versionName = "([^"]*)".*/\1/')"
  version_code="$(grep -m1 'versionCode' "$REPO_ROOT/$module/build.gradle.kts" | sed -E 's/[^0-9]*([0-9]+).*/\1/')"
  dest="$DIST_DIR/${module}-release-v${version_code}-${version_name}.apk"
  cp "$apk" "$dest"

  echo "[$module] sha256: $(shasum -a 256 "$dest" | awk '{print $1}')"
  echo "[$module] -> $dest"

  if [ "$DO_PUBLISH" = "1" ]; then
    publish_apk "$module" "$dest"
  fi
done

echo "Done."
