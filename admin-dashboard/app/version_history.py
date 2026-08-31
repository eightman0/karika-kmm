from datetime import datetime

from . import local_db
from .tz import LOCAL_TZ


def record_publish(
    app: str,
    version_code: str,
    version_name: str,
    apk_url: str,
    apk_sha256: str,
    mandatory: bool,
    published_by: str,
) -> None:
    local_db.record_publish(app, int(version_code), version_name, apk_url, apk_sha256, mandatory, published_by)


def get_history(app: str, limit: int = 10) -> list[dict]:
    rows = local_db.get_history(app, limit)
    return [
        {
            "id": row["id"],
            "app": row["app"],
            "versionCode": row["version_code"],
            "versionName": row["version_name"],
            "apkUrl": row["apk_url"],
            "apkSha256": row["apk_sha256"],
            "mandatory": bool(row["mandatory"]),
            "publishedBy": row["published_by"],
            "publishedAt": (
                datetime.fromisoformat(row["published_at"]).astimezone(LOCAL_TZ)
                if row["published_at"] else None
            ),
        }
        for row in rows
    ]


def delete_entry(entry_id: int) -> None:
    local_db.delete_history_entry(entry_id)


def get_available_versions(app: str) -> list[dict]:
    """One entry per version_code (the most recent publish of it, in case a code got reused),
    newest first - for the "which version to send" picker on the devices pages."""
    seen = {}
    for h in get_history(app, limit=200):
        seen.setdefault(h["versionCode"], h)
    return sorted(seen.values(), key=lambda h: h["versionCode"], reverse=True)
