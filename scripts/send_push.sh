#!/usr/bin/env bash
# Salje FCM push (HTTP v1) na jedan device token koristeci service account JSON.
# Upotreba:
#   ./scripts/send_push.sh <device_token> [title] [body] [route]
#   ./scripts/send_push.sh --token-only   # samo ispise OAuth access token (za Postman)
# Po defaultu koristi service account iz scripts/ (*firebase-adminsdk*.json); drugi se moze dati preko SA_FILE=...
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SA_FILE="${SA_FILE:-$(ls "$SCRIPT_DIR"/*firebase-adminsdk*.json 2>/dev/null | head -1)}"
[[ -f "$SA_FILE" ]] || { echo "Nema service account JSON-a u $SCRIPT_DIR (ili postavi SA_FILE=...)" >&2; exit 1; }

DEVICE_TOKEN="${1:?device token ili --token-only}"
TITLE="${2:-Test notifikacija}"
BODY="${3:-Ovo je test push}"
ROUTE="${4:-}"

field() { python3 -c "import json,sys; print(json.load(open(sys.argv[1]))[sys.argv[2]])" "$SA_FILE" "$1"; }
b64url() { openssl base64 -e -A | tr '+/' '-_' | tr -d '='; }

CLIENT_EMAIL="$(field client_email)"
PROJECT_ID="$(field project_id)"
KEY_FILE="$(mktemp)"; trap 'rm -f "$KEY_FILE"' EXIT
field private_key > "$KEY_FILE"

NOW=$(date +%s)
HEADER=$(printf '{"alg":"RS256","typ":"JWT"}' | b64url)
CLAIMS=$(printf '{"iss":"%s","scope":"https://www.googleapis.com/auth/firebase.messaging","aud":"https://oauth2.googleapis.com/token","iat":%d,"exp":%d}' \
  "$CLIENT_EMAIL" "$NOW" "$((NOW + 3600))" | b64url)
SIG=$(printf '%s.%s' "$HEADER" "$CLAIMS" | openssl dgst -sha256 -sign "$KEY_FILE" | b64url)
JWT="$HEADER.$CLAIMS.$SIG"

ACCESS_TOKEN=$(curl -s https://oauth2.googleapis.com/token \
  -d grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer -d assertion="$JWT" \
  | python3 -c "import json,sys; r=json.load(sys.stdin); print(r.get('access_token') or sys.exit('OAuth greska: '+json.dumps(r)))")

if [[ "$DEVICE_TOKEN" == "--token-only" ]]; then
  echo "project_id: $PROJECT_ID"
  echo "$ACCESS_TOKEN"
  exit 0
fi

PAYLOAD=$(python3 - "$DEVICE_TOKEN" "$TITLE" "$BODY" "$ROUTE" <<'PY'
import json, sys
token, title, body, route = sys.argv[1:]
msg = {"token": token,
       "notification": {"title": title, "body": body},
       "android": {"priority": "high"}}
if route:
    msg["data"] = {"route": route}
print(json.dumps({"message": msg}))
PY
)

curl -s -X POST "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD"
echo
