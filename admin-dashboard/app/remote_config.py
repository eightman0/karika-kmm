"""
Publishes the salesrep version parameters the launcher's silent-update pipeline reads.

The Python Admin SDK's firebase_admin.remote_config module only covers the newer "Remote Config
for servers" feature - it has no get_template/publish_template for the classic, client-facing
template that Android's fetchAndActivate() reads. That only exists in the Node.js and Java Admin
SDKs. So this talks to the REST API directly, authenticated with an OAuth token minted from the
same service account credentials used for Firestore/Storage.
"""

import os

import requests
from dotenv import load_dotenv
from google.auth.transport.requests import Request
from google.oauth2 import service_account

load_dotenv()

SCOPES = ["https://www.googleapis.com/auth/firebase.remoteconfig"]
TEMPLATE_URL = "https://firebaseremoteconfig.googleapis.com/v1/projects/{project_id}/remoteConfig"

PARAM_VERSION_CODE = "kiosk_version_code"
PARAM_VERSION_NAME = "kiosk_version_name"
PARAM_APK_URL = "kiosk_apk_url"
PARAM_APK_SHA256 = "kiosk_apk_sha256"
PARAM_MANDATORY = "kiosk_mandatory"


def _access_token() -> str:
    creds = service_account.Credentials.from_service_account_file(
        os.environ["FIREBASE_SERVICE_ACCOUNT_PATH"], scopes=SCOPES
    )
    creds.refresh(Request())
    return creds.token


def _template_url() -> str:
    return TEMPLATE_URL.format(project_id=os.environ["FIREBASE_PROJECT_ID"])


def _get_template() -> tuple[dict, str]:
    """Returns (template_json, etag). The etag is required by the PUT below - Remote Config
    uses it for optimistic concurrency so a publish here can't silently clobber a concurrent
    edit made in the Firebase console."""
    resp = requests.get(_template_url(), headers={"Authorization": f"Bearer {_access_token()}"})
    resp.raise_for_status()
    return resp.json(), resp.headers["ETag"]


def get_kiosk_version() -> dict:
    template, _ = _get_template()
    params = template.get("parameters", {})

    def value(key: str, default: str = "") -> str:
        return params.get(key, {}).get("defaultValue", {}).get("value", default)

    return {
        "version_code": value(PARAM_VERSION_CODE, "0"),
        "version_name": value(PARAM_VERSION_NAME),
        "apk_url": value(PARAM_APK_URL),
        "apk_sha256": value(PARAM_APK_SHA256),
        "mandatory": value(PARAM_MANDATORY, "false"),
    }


def publish_kiosk_version(
    version_code: str, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool
) -> None:
    template, etag = _get_template()
    params = template.setdefault("parameters", {})

    def set_value(key: str, value: str) -> None:
        params[key] = {"defaultValue": {"value": value}, "valueType": "STRING"}

    set_value(PARAM_VERSION_CODE, str(version_code))
    set_value(PARAM_VERSION_NAME, version_name)
    set_value(PARAM_APK_URL, apk_url)
    set_value(PARAM_APK_SHA256, apk_sha256)
    set_value(PARAM_MANDATORY, "true" if mandatory else "false")

    resp = requests.put(
        _template_url(),
        headers={
            "Authorization": f"Bearer {_access_token()}",
            "Content-Type": "application/json; UTF8",
            "If-Match": etag,
        },
        json=template,
    )
    resp.raise_for_status()
