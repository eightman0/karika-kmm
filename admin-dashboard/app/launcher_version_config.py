"""
Mirrors version_config.py exactly, but for the launcher instead of salesrep - see local_db.py's
schema comment for why this is a separate parallel module/tables rather than a generalized one:
the existing kiosk_version/staged_kiosk_version tables are load-bearing for the live fleet's
salesrep updates, so a second app gets its own pair instead of risking a migration of those.

version_history stays shared (it already has an `app` column) - only record_publish(app="launcher",
...) differs from the salesrep call site.
"""

from . import local_db
from .version_config import _shape


def get_launcher_version() -> dict:
    return _shape(local_db.get_launcher_version_row())


def get_staged_launcher_version() -> dict:
    return _shape(local_db.get_staged_launcher_version_row())


def is_launcher_staged() -> bool:
    return get_staged_launcher_version()["version_code"] not in ("0", "", None)


def staged_launcher_target_count() -> int:
    return local_db.count_staged_launcher_targets()


def highest_known_launcher_version_code() -> str:
    return str(
        max(int(get_launcher_version()["version_code"]), int(get_staged_launcher_version()["version_code"]))
    )


def publish_staged_launcher_version(
    version_code: str, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool,
    published_by: str,
) -> None:
    local_db.set_staged_launcher_version(
        int(version_code), version_name, apk_url, apk_sha256, mandatory, published_by
    )


def promote_staged_launcher_to_stable(published_by: str) -> str:
    staged = local_db.get_staged_launcher_version_row()
    if not staged:
        return get_launcher_version()["version_code"]
    local_db.set_launcher_version(
        staged["version_code"], staged["version_name"], staged["apk_url"],
        staged["apk_sha256"], bool(staged["mandatory"]), published_by,
    )
    local_db.clear_staged_launcher_version()
    return str(staged["version_code"])


def stage_launcher_version_by_code(version_code: str, published_by: str) -> None:
    entry = local_db.get_history_entry_by_version("launcher", int(version_code))
    if not entry:
        raise ValueError(f"Nema objavljene verzije launcher-a sa version code {version_code}")
    local_db.set_staged_launcher_version(
        entry["version_code"], entry["version_name"], entry["apk_url"],
        entry["apk_sha256"], bool(entry["mandatory"]), published_by,
    )


def rollback_launcher_to_history_entry(entry_id: int, published_by: str) -> str:
    entry = local_db.get_history_entry_by_id(entry_id)
    if not entry:
        raise ValueError(f"Nema publish zapisa sa id={entry_id}")
    local_db.set_launcher_version(
        entry["version_code"], entry["version_name"], entry["apk_url"],
        entry["apk_sha256"], bool(entry["mandatory"]), published_by,
    )
    return str(entry["version_code"])


def resolve_launcher_version_for_device(device_id: str | None) -> dict:
    if device_id and is_launcher_staged() and local_db.is_staged_launcher_target(device_id):
        return get_staged_launcher_version()
    return get_launcher_version()


def target_device_for_staged_launcher(device_id: str) -> None:
    local_db.add_staged_launcher_target(device_id)
