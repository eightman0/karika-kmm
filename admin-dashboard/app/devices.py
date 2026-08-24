from datetime import datetime, timedelta, timezone

from google.cloud.firestore_v1 import SERVER_TIMESTAMP

from .firebase import bucket, db

STALE_AFTER_SECONDS = 12 * 60 * 60  # 12h - covers the 6h periodic worker plus one missed cycle
SIGNED_URL_MINUTES = 30

APP_PACKAGES = {
    "salesrep": "karika.distribucija.ba.salesrep",
}


def list_devices() -> list[dict]:
    devices = []
    for doc in db().collection("devices").stream():
        devices.append(_with_computed_fields(doc.id, doc.to_dict() or {}))

    devices.sort(key=lambda d: d["lastSeenAt"] or datetime.min.replace(tzinfo=timezone.utc), reverse=True)
    return devices


def filter_devices(all_devices: list[dict], query: str = "", app_filter: str = "all") -> list[dict]:
    result = all_devices
    if query:
        needle = query.lower()
        result = [d for d in result if needle in d["id"].lower()]
    if app_filter != "all":
        package = APP_PACKAGES.get(app_filter, app_filter)
        result = [d for d in result if d.get("installedPackage") == package]
    return result


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
    doc = db().collection("devices").document(device_id).get()
    if not doc.exists:
        return None
    return _with_computed_fields(doc.id, doc.to_dict() or {})


def request_logs(device_id: str) -> None:
    db().collection("devices").document(device_id).set(
        {"logRequestedAt": SERVER_TIMESTAMP}, merge=True
    )


def signed_log_url(storage_path: str) -> str:
    blob = bucket().blob(storage_path)
    return blob.generate_signed_url(expiration=timedelta(minutes=SIGNED_URL_MINUTES))


def _with_computed_fields(device_id: str, data: dict) -> dict:
    data["id"] = device_id
    data["status"] = _status(data.get("lastSeenAt"))
    return data


def _status(last_seen) -> str:
    if last_seen is None:
        return "never"
    age = datetime.now(timezone.utc) - last_seen
    return "online" if age.total_seconds() < STALE_AFTER_SECONDS else "stale"
