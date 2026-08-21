from datetime import datetime, timedelta, timezone

from google.cloud.firestore_v1 import SERVER_TIMESTAMP

from .firebase import bucket, db

STALE_AFTER_SECONDS = 12 * 60 * 60  # 12h - covers the 6h periodic worker plus one missed cycle
SIGNED_URL_MINUTES = 30


def list_devices() -> list[dict]:
    devices = []
    for doc in db().collection("devices").stream():
        devices.append(_with_computed_fields(doc.id, doc.to_dict() or {}))

    devices.sort(key=lambda d: d["lastSeenAt"] or datetime.min.replace(tzinfo=timezone.utc), reverse=True)
    return devices


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
