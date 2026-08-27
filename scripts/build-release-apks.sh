#!/usr/bin/env bash
# Builds signed release APKs for launcher and/or salesrep using each module's own
# keystore.properties (see launcher/keystore.properties, salesrep/keystore.properties) - the same
# config Gradle already picks up automatically, just invoked headlessly instead of through Android
# Studio's "Generate Signed Bundle/APK" wizard.
#
# Building salesrep also stages the APK on the admin dashboard (POST /versions/publish) unless
# --no-publish is given - same as uploading it by hand on the /versions page, just scripted. This
# only stages it (see version_config.py) - no device installs anything until it's explicitly sent
# to chosen devices or to everyone, so it's safe to run on every build. Needs
# KARIKA_DASHBOARD_URL/KARIKA_ADMIN_USERNAME/KARIKA_ADMIN_PASSWORD - either exported already or
# defined in scripts/publish.env (gitignored, see scripts/publish.env.example).
#
# Launcher isn't wired into that flow - it's Device Owner, updated only by re-provisioning with a
# fresh QR scan, and downloaded during provisioning straight from a fixed Firebase Storage path
# (see admin-dashboard/app/provisioning.py) that has to be updated by hand.
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

publish_salesrep() {
  local apk="$1"

  if [ -f "$REPO_ROOT/scripts/publish.env" ]; then
    # shellcheck disable=SC1091
    source "$REPO_ROOT/scripts/publish.env"
  fi
  local url="${KARIKA_DASHBOARD_URL:-}"
  local user="${KARIKA_ADMIN_USERNAME:-}"
  local pass="${KARIKA_ADMIN_PASSWORD:-}"
  if [ -z "$url" ] || [ -z "$user" ] || [ -z "$pass" ]; then
    echo "[salesrep] skipping dashboard publish: KARIKA_DASHBOARD_URL/KARIKA_ADMIN_USERNAME/KARIKA_ADMIN_PASSWORD" >&2
    echo "[salesrep] not set - export them, or copy scripts/publish.env.example to scripts/publish.env" >&2
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
      echo "[salesrep] dashboard login failed (redirected to: $login_redirect) - check credentials" >&2
      return 1
      ;;
  esac

  local publish_redirect
  publish_redirect="$(curl -fsS -o /dev/null -w '%{redirect_url}' -b "$cookie_jar" \
    -F "apk_file=@${apk};type=application/vnd.android.package-archive" \
    "$url/versions/publish")"
  case "$publish_redirect" in
    *error=*)
      echo "[salesrep] dashboard publish failed: $publish_redirect" >&2
      return 1
      ;;
  esac
  echo "[salesrep] staged on dashboard as the pending version - use the devices list or"
  echo "[salesrep] \"Posalji svima\" on $url/versions to actually send it to devices"
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

  if [ "$module" = "salesrep" ] && [ "$DO_PUBLISH" = "1" ]; then
    publish_salesrep "$dest"
  fi
done

echo "Done."
