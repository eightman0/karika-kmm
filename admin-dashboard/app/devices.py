from datetime import datetime, timedelta, timezone

from . import local_db
from .firebase import bucket
from .push import (
    send_analytics_request_all,
    send_factory_reset,
    send_log_request,
    send_maintenance,
    send_open_settings,
    send_ping,
    send_reboot,
    send_version_check_all,
    send_version_check_to_device,
)
from .tz import LOCAL_TZ
from .version_config import (
    get_staged_version,
    promote_staged_to_stable,
    target_device_for_staged,
)

STALE_AFTER_SECONDS = 12 * 60 * 60  # 12h - covers the 30min periodic worker plus a lot of slack
SIGNED_URL_MINUTES = 30

APP_PACKAGES = {
    "salesrep": "karika.distribucija.ba.salesrep",
}


def _parse_iso(value: str | None) -> datetime | None:
    # Stored (and sorted/diffed elsewhere) as UTC - only the display timezone changes here, the
    # underlying instant, and so every comparison/arithmetic done against it, is unaffected.
    return datetime.fromisoformat(value).astimezone(LOCAL_TZ) if value else None


def _with_computed_fields(row: dict) -> dict:
    return {
        "id": row["id"],
        "installedPackage": row["installed_package"],
        "installedVersionCode": row["installed_version_code"],
        "installedVersionName": row["installed_version_name"],
        "androidSdkInt": row["android_sdk_int"],
        "androidRelease": row["android_release"],
        "deviceModel": row["device_model"],
        "lastSeenAt": _parse_iso(row["last_seen_at"]),
        "logRequestedAt": _parse_iso(row["log_requested_at"]),
        "lastLogUploadUrl": row["last_log_upload_url"],
        "lastLogUploadPath": row["last_log_upload_path"],
        "lastLogUploadAt": _parse_iso(row["last_log_upload_at"]),
        "lastLogUploadRequestHandledAt": _parse_iso(row["last_log_upload_request_handled_at"]),
        "lastAnalyticsUploadUrl": row["last_analytics_upload_url"],
        "lastAnalyticsUploadPath": row["last_analytics_upload_path"],
        "lastAnalyticsUploadAt": _parse_iso(row["last_analytics_upload_at"]),
        "customerId": row["customer_id"],
        "siteId": row["site_id"],
        "lastLoginEmail": row["last_login_email"],
        "lastLoginAt": _parse_iso(row["last_login_at"]),
        "status": _status(_parse_iso(row["last_seen_at"])),
        # None (not False) until a heartbeat from a launcher build new enough to report it comes
        # in - showing "not in maintenance" for a device we simply have no answer from yet would
        # be worse than showing nothing.
        "maintenanceActive": bool(row["maintenance_active"]) if row["maintenance_active"] is not None else None,
        "pingRequestedAt": _parse_iso(row["ping_requested_at"]),
    }


def list_devices() -> list[dict]:
    all_devices = [_with_computed_fields(row) for row in local_db.list_devices()]
    all_devices.sort(key=lambda d: d["lastSeenAt"] or datetime.min.replace(tzinfo=timezone.utc), reverse=True)
    return all_devices


def filter_devices(all_devices: list[dict], query: str = "") -> list[dict]:
    if not query:
        return all_devices
    needle = query.lower()
    return [d for d in all_devices if needle in d["id"].lower()]


def fleet_summary(all_devices: list[dict]) -> dict:
    return {
        "total": len(all_devices),
        "online": sum(1 for d in all_devices if d["status"] == "online"),
        "stale": sum(1 for d in all_devices if d["status"] == "stale"),
        "never": sum(1 for d in all_devices if d["status"] == "never"),
    }


def count_on_version(all_devices: list[dict], package_name: str, version_code) -> int:
    target = str(version_code)
    return sum(
        1
        for d in all_devices
        if d.get("installedPackage") == package_name and str(d.get("installedVersionCode")) == target
    )


def get_device(device_id: str) -> dict | None:
    row = local_db.get_device(device_id)
    return _with_computed_fields(row) if row else None


def delete_device(device_id: str) -> None:
    local_db.delete_device(device_id)


def request_logs(device_id: str) -> None:
    requested_at = local_db.request_log_pull(device_id)
    # The device pulls the actual log content itself once it wakes up - this is only the "please
    # do that now" nudge, same push channel the silent-update check already uses instead of a
    # Firestore listener.
    send_log_request(device_id, requested_at)


def _require_token(device_id: str) -> str:
    device = local_db.get_device(device_id)
    token = device.get("fcm_token") if device else None
    if not token:
        raise ValueError(
            "Uređaj nema poznat FCM token - mora se prvo javiti dashboardu (heartbeat) "
            "nakon zadnjeg pokretanja."
        )
    return token


def request_factory_reset(device_id: str) -> None:
    send_factory_reset(_require_token(device_id))


def request_reboot(device_id: str) -> None:
    send_reboot(_require_token(device_id))


def request_maintenance(device_id: str, enable: bool) -> None:
    send_maintenance(_require_token(device_id), enable)


def request_open_settings(device_id: str) -> None:
    send_open_settings(_require_token(device_id))


def request_ping(device_id: str) -> None:
    local_db.request_ping(device_id)
    send_ping(_require_token(device_id))


def request_update_check(device_id: str) -> None:
    target_device_for_staged(device_id)
    send_version_check_to_device(device_id, get_staged_version()["version_code"])


def request_update_check_bulk(device_ids: list[str]) -> None:
    version_code = get_staged_version()["version_code"]
    for device_id in device_ids:
        target_device_for_staged(device_id)
        send_version_check_to_device(device_id, version_code)


def request_update_all() -> None:
    version_code = promote_staged_to_stable()
    send_version_check_all(version_code)


def request_analytics_all() -> None:
    send_analytics_request_all()


def set_device_mapping(device_id: str, customer_id: str, site_id: str) -> None:
    local_db.set_device_mapping(device_id, customer_id or None, site_id or None)


def command_log(device_id: str, limit: int = 20) -> list[dict]:
    return local_db.get_command_log(device_id, limit)


def last_ping_ack(device_id: str) -> dict | None:
    for entry in local_db.get_command_log(device_id, limit=50):
        if entry["command"] == "ping":
            return {**entry, "created_at": _parse_iso(entry["created_at"])}
    return None


def signed_log_url(storage_path: str) -> str:
    blob = bucket().blob(storage_path)
    return blob.generate_signed_url(expiration=timedelta(minutes=SIGNED_URL_MINUTES))


def signed_analytics_url(storage_path: str) -> str:
    blob = bucket().blob(storage_path)
    return blob.generate_signed_url(expiration=timedelta(minutes=SIGNED_URL_MINUTES))


def _status(last_seen: datetime | None) -> str:
    if last_seen is None:
        return "never"
    age = datetime.now(timezone.utc) - last_seen
    return "online" if age.total_seconds() < STALE_AFTER_SECONDS else "stale"
