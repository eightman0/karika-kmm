from datetime import datetime

from . import apk_storage, launcher_version_config, local_db, version_config
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
    """Deletes the history row, then the Storage blob it published too - but only once nothing
    else still needs that exact app+version_code blob: another history row for the same reused
    version_code (see get_history_entry_by_version's own comment on why that can happen), or the
    live/staged pointer either app currently serves to devices. Deleting a blob still referenced by
    one of those would 404 every device that fetches it next, including new ones being provisioned
    right now."""
    entry = local_db.get_history_entry_by_id(entry_id)
    if not entry:
        return
    local_db.delete_history_entry(entry_id)
    if _blob_still_needed(entry["app"], entry["version_code"]):
        return
    apk_storage.delete_apk(entry["app"], str(entry["version_code"]))


def _blob_still_needed(app: str, version_code: int) -> bool:
    if local_db.get_history_entry_by_version(app, version_code):
        return True
    version_code_str = str(version_code)
    if app == "launcher":
        live = launcher_version_config.get_launcher_version()
        staged = launcher_version_config.get_staged_launcher_version()
    else:
        live = version_config.get_kiosk_version()
        staged = version_config.get_staged_version()
    return version_code_str in (live["version_code"], staged["version_code"])


def get_available_versions(app: str) -> list[dict]:
    """One entry per version_code (the most recent publish of it, in case a code got reused),
    newest first - for the "which version to send" picker on the devices pages."""
    seen = {}
    for h in get_history(app, limit=200):
        seen.setdefault(h["versionCode"], h)
    return sorted(seen.values(), key=lambda h: h["versionCode"], reverse=True)
