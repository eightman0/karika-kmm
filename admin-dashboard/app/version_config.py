"""
Publishing writes a *staged* salesrep build - visible only to devices explicitly targeted from
the devices list/detail pages (see devices.request_update_check()), never through the normal
/api/version poll every other device makes. Nothing else changes what those other devices see
until "Posalji svima" promotes the staged build to stable - the version every device gets by
default, immediately (broadcast push) or on its next periodic poll either way.

Backed by local_db (SQLite), not Firestore - see local_db.py for why.
"""

from . import local_db


def _shape(row: dict | None) -> dict:
    row = row or {}
    return {
        "version_code": str(row.get("version_code") or "0"),
        "version_name": row.get("version_name") or "",
        "apk_url": row.get("apk_url") or "",
        "apk_sha256": row.get("apk_sha256") or "",
        "mandatory": "true" if row.get("mandatory", 1) else "false",
    }


def get_kiosk_version() -> dict:
    return _shape(local_db.get_kiosk_version_row())


def get_staged_version() -> dict:
    return _shape(local_db.get_staged_kiosk_version_row())


def is_staged() -> bool:
    return get_staged_version()["version_code"] not in ("0", "", None)


def staged_target_count() -> int:
    return local_db.count_staged_targets()


def highest_known_version_code() -> str:
    """For the devices list "zaostaje" flag - a device that already got the staged build is ahead
    of stable, not behind it, so lagging has to mean "behind whichever of stable/staged is newer",
    not just "not equal to stable"."""
    return str(max(int(get_kiosk_version()["version_code"]), int(get_staged_version()["version_code"])))


def publish_staged_version(
    version_code: str, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool
) -> None:
    local_db.set_staged_kiosk_version(int(version_code), version_name, apk_url, apk_sha256, mandatory)


def promote_staged_to_stable() -> str:
    """Makes the staged build the one every device gets by default. Returns its version_code (for
    the caller to broadcast an immediate-check push), or the current stable version_code unchanged
    if nothing was staged."""
    staged = local_db.get_staged_kiosk_version_row()
    if not staged:
        return get_kiosk_version()["version_code"]
    local_db.set_kiosk_version(
        staged["version_code"], staged["version_name"], staged["apk_url"],
        staged["apk_sha256"], bool(staged["mandatory"]),
    )
    local_db.clear_staged_kiosk_version()
    return str(staged["version_code"])


def resolve_version_for_device(device_id: str | None) -> dict:
    """What a specific device should see when it polls /api/version - the staged build if one
    exists and this device was explicitly targeted, otherwise the stable build everyone else is
    on. device_id=None (older launcher builds that predate per-device targeting) always gets
    stable, same as an untargeted device."""
    if device_id and is_staged() and local_db.is_staged_target(device_id):
        return get_staged_version()
    return get_kiosk_version()


def target_device_for_staged(device_id: str) -> None:
    local_db.add_staged_target(device_id)
