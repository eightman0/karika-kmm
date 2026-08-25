from datetime import datetime, timedelta, timezone

from . import local_db
from .firebase import bucket
from .push import send_location_request, send_location_request_all, send_log_request

STALE_AFTER_SECONDS = 12 * 60 * 60  # 12h - covers the 30min periodic worker plus a lot of slack
SIGNED_URL_MINUTES = 30

APP_PACKAGES = {
    "salesrep": "karika.distribucija.ba.salesrep",
}


def _parse_iso(value: str | None) -> datetime | None:
    return datetime.fromisoformat(value) if value else None


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
        "locationLat": row["location_lat"],
        "locationLng": row["location_lng"],
        "locationAccuracy": row["location_accuracy"],
        "locationAt": _parse_iso(row["location_at"]),
        "locationRequestedAt": _parse_iso(row["location_requested_at"]),
        "status": _status(_parse_iso(row["last_seen_at"])),
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


def request_location(device_id: str) -> None:
    local_db.request_device_location(device_id)
    send_location_request(device_id)


def request_all_locations() -> None:
    local_db.request_all_device_locations()
    send_location_request_all()


def signed_log_url(storage_path: str) -> str:
    blob = bucket().blob(storage_path)
    return blob.generate_signed_url(expiration=timedelta(minutes=SIGNED_URL_MINUTES))


def _status(last_seen: datetime | None) -> str:
    if last_seen is None:
        return "never"
    age = datetime.now(timezone.utc) - last_seen
    return "online" if age.total_seconds() < STALE_AFTER_SECONDS else "stale"
