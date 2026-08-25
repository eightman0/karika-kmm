"""
Publishes the salesrep version info the launcher's silent-update pipeline reads, and sends an FCM
data message so devices check for it right away instead of waiting out the next periodic poll.

Backed by local_db (SQLite), not Firestore - see local_db.py for why.
"""

from . import local_db
from .push import send_version_check


def get_kiosk_version() -> dict:
    row = local_db.get_kiosk_version_row() or {}
    return {
        "version_code": str(row.get("version_code") or "0"),
        "version_name": row.get("version_name") or "",
        "apk_url": row.get("apk_url") or "",
        "apk_sha256": row.get("apk_sha256") or "",
        "mandatory": "true" if row.get("mandatory", 1) else "false",
    }


def publish_kiosk_version(
    version_code: str, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool
) -> None:
    local_db.set_kiosk_version(int(version_code), version_name, apk_url, apk_sha256, mandatory)
    send_version_check(version_code)
