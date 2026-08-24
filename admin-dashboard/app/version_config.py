"""
Publishes the salesrep version info the launcher's silent-update pipeline reads, and sends an FCM
data message so devices check for it right away instead of waiting out the next periodic poll.

Firestore (not Remote Config) is the source of truth: a single doc at config/kiosk_version, read
by the launcher with a plain one-shot get() - no live listener - so it works whether or not the
launcher process happens to be frozen when the check runs.
"""

import logging

from firebase_admin import messaging

from .firebase import db

FCM_TOPIC = "kiosk-updates"

logger = logging.getLogger(__name__)


def _doc():
    return db().collection("config").document("kiosk_version")


def get_kiosk_version() -> dict:
    data = _doc().get().to_dict() or {}
    return {
        "version_code": str(data.get("versionCode", "0")),
        "version_name": data.get("versionName", ""),
        "apk_url": data.get("apkUrl", ""),
        "apk_sha256": data.get("apkSha256", ""),
        "mandatory": "true" if data.get("mandatory", True) else "false",
    }


def publish_kiosk_version(
    version_code: str, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool
) -> None:
    _doc().set(
        {
            "versionCode": int(version_code),
            "versionName": version_name,
            "apkUrl": apk_url,
            "apkSha256": apk_sha256,
            "mandatory": mandatory,
        }
    )
    try:
        messaging.send(messaging.Message(topic=FCM_TOPIC, data={"versionCode": str(version_code)}))
    except Exception:
        # The periodic on-device check picks this up within ~30 min regardless, so a push
        # failure (e.g. a transient FCM hiccup) shouldn't block the publish itself.
        logger.exception("Failed to send FCM update-check ping")
