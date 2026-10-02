"""
Same staged/stable publish flow as version_config.py (salesrep) and launcher_version_config.py,
but keyed by app over the shared app_version/app_version_targets tables - for every payload app
onboarded after those two (shop so far), so each new one doesn't need its own parallel module and
singleton tables. See local_db.py's schema comment.

version_history is shared as always - record_publish(app=..., ...) is the only per-app difference.
"""

from . import local_db
from .version_config import _shape

APPS = ("shop",)

STABLE = "stable"
STAGED = "staged"


def get_version(app: str) -> dict:
    return _shape(local_db.get_app_version_row(app, STABLE))


def get_staged_version(app: str) -> dict:
    return _shape(local_db.get_app_version_row(app, STAGED))


def is_staged(app: str) -> bool:
    return get_staged_version(app)["version_code"] not in ("0", "", None)


def staged_target_count(app: str) -> int:
    return local_db.count_app_version_targets(app)


def highest_known_version_code(app: str) -> str:
    return str(max(int(get_version(app)["version_code"]), int(get_staged_version(app)["version_code"])))


def publish_staged_version(
    app: str, version_code: str, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool,
    published_by: str,
) -> None:
    local_db.set_app_version(
        app, STAGED, int(version_code), version_name, apk_url, apk_sha256, mandatory, published_by
    )


def promote_staged_to_stable(app: str, published_by: str) -> str:
    staged = local_db.get_app_version_row(app, STAGED)
    if not staged:
        return get_version(app)["version_code"]
    local_db.set_app_version(
        app, STABLE, staged["version_code"], staged["version_name"], staged["apk_url"],
        staged["apk_sha256"], bool(staged["mandatory"]), published_by,
    )
    local_db.clear_app_version(app, STAGED)
    return str(staged["version_code"])


def stage_version_by_code(app: str, version_code: str, published_by: str) -> None:
    entry = local_db.get_history_entry_by_version(app, int(version_code))
    if not entry:
        raise ValueError(f"Nema objavljene verzije za {app} sa version code {version_code}")
    local_db.set_app_version(
        app, STAGED, entry["version_code"], entry["version_name"], entry["apk_url"],
        entry["apk_sha256"], bool(entry["mandatory"]), published_by,
    )


def rollback_to_history_entry(app: str, entry_id: int, published_by: str) -> str:
    entry = local_db.get_history_entry_by_id(entry_id)
    if not entry:
        raise ValueError(f"Nema publish zapisa sa id={entry_id}")
    local_db.set_app_version(
        app, STABLE, entry["version_code"], entry["version_name"], entry["apk_url"],
        entry["apk_sha256"], bool(entry["mandatory"]), published_by,
    )
    return str(entry["version_code"])


def resolve_version_for_device(app: str, device_id: str | None) -> dict:
    if device_id and is_staged(app) and local_db.is_app_version_target(app, device_id):
        return get_staged_version(app)
    return get_version(app)


def target_device_for_staged(app: str, device_id: str) -> None:
    local_db.add_app_version_target(app, device_id)
