from datetime import date, datetime, time, timedelta, timezone

from . import launcher_version_config, local_db
from .firebase import bucket
from .push import (
    send_analytics_request_all,
    send_debug_unlock,
    send_factory_reset,
    send_log_request,
    send_maintenance,
    send_open_settings,
    send_ping,
    send_reboot,
    send_update_launcher_to_device,
    send_version_check_all,
    send_version_check_to_device,
)
from .tz import LOCAL_TZ
from .version_config import (
    get_staged_version,
    promote_staged_to_stable,
    stage_version_by_code,
    target_device_for_staged,
)

STALE_AFTER_SECONDS = 12 * 60 * 60  # 12h - covers the 30min periodic worker plus a lot of slack
SIGNED_URL_MINUTES = 30

APP_PACKAGES = {
    "salesrep": "karika.distribucija.ba.salesrep",
    "launcher": "karika.distribucija.ba.launcher",
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
        "batteryLevel": row["battery_level"],
        "batteryCharging": bool(row["battery_charging"]) if row["battery_charging"] is not None else None,
        "launcherVersionCode": row["launcher_version_code"],
        "launcherVersionName": row["launcher_version_name"],
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


def request_debug_unlock(device_id: str, enable: bool) -> None:
    send_debug_unlock(_require_token(device_id), enable)


def request_open_settings(device_id: str) -> None:
    send_open_settings(_require_token(device_id))


def request_update_launcher(device_id: str, version_code: str | None, published_by: str) -> None:
    if version_code:
        launcher_version_config.stage_launcher_version_by_code(version_code, published_by)
    launcher_version_config.target_device_for_staged_launcher(device_id)
    send_update_launcher_to_device(
        _require_token(device_id), launcher_version_config.get_staged_launcher_version()["version_code"]
    )


def request_update_launcher_bulk(device_ids: list[str], version_code: str | None, published_by: str) -> None:
    if version_code:
        launcher_version_config.stage_launcher_version_by_code(version_code, published_by)
    resolved_version_code = launcher_version_config.get_staged_launcher_version()["version_code"]
    for device_id in device_ids:
        launcher_version_config.target_device_for_staged_launcher(device_id)
        row = local_db.get_device(device_id)
        token = row.get("fcm_token") if row else None
        if token:
            send_update_launcher_to_device(token, resolved_version_code)


def request_update_launcher_all(published_by: str) -> None:
    # Token-based to every known device, not a topic broadcast like salesrep's send_version_
    # check_all - the launcher has no periodic self-check to fall back on if a topic message is
    # missed (see KioskMessagingService: it only ever self-updates on an explicit push), so a
    # missed broadcast would strand that device on the old build indefinitely.
    version_code = launcher_version_config.promote_staged_launcher_to_stable(published_by)
    for row in local_db.list_devices():
        token = row.get("fcm_token")
        if token:
            send_update_launcher_to_device(token, version_code)


def request_ping(device_id: str) -> None:
    local_db.request_ping(device_id)
    send_ping(_require_token(device_id))


def request_update_check(device_id: str, version_code: str | None, published_by: str) -> None:
    if version_code:
        stage_version_by_code(version_code, published_by)
    target_device_for_staged(device_id)
    send_version_check_to_device(_require_token(device_id), get_staged_version()["version_code"])


def request_update_check_bulk(device_ids: list[str], version_code: str | None, published_by: str) -> None:
    if version_code:
        stage_version_by_code(version_code, published_by)
    resolved_version_code = get_staged_version()["version_code"]
    for device_id in device_ids:
        target_device_for_staged(device_id)
        row = local_db.get_device(device_id)
        token = row.get("fcm_token") if row else None
        if token:
            # No known token yet just means it's still targeted - its own periodic poll picks up
            # the staged version regardless, so skip the immediate push rather than stop the batch.
            send_version_check_to_device(token, resolved_version_code)


def request_update_all(published_by: str) -> None:
    version_code = promote_staged_to_stable(published_by)
    send_version_check_all(version_code)


def request_analytics_all() -> None:
    send_analytics_request_all()


def set_device_mapping(device_id: str, customer_id: str, site_id: str) -> None:
    local_db.set_device_mapping(device_id, customer_id or None, site_id or None)


def command_log(device_id: str, limit: int = 20) -> list[dict]:
    return local_db.get_command_log(device_id, limit)


def latest_location(device_id: str) -> dict | None:
    row = local_db.get_latest_location(device_id)
    if not row:
        return None
    return {"lat": row["lat"], "lon": row["lon"], "ts": _parse_iso(row["ts"])}


def locations_for_day(device_id: str, date_str: str | None) -> tuple[str, list[dict]]:
    """Resolves date_str (a "?date=" query param, possibly missing/invalid) to a calendar day and
    returns that day's GPS points in chronological order. Day boundaries are computed in
    Sarajevo-local time, not UTC (a device's "today" shouldn't shift with the UTC offset) - see
    the same reasoning in local_db.events_per_day for why this can't just be a SQL date() bucket.
    """
    try:
        day = date.fromisoformat(date_str) if date_str else datetime.now(LOCAL_TZ).date()
    except ValueError:
        day = datetime.now(LOCAL_TZ).date()
    start_local = datetime.combine(day, time.min, tzinfo=LOCAL_TZ)
    end_local = start_local + timedelta(days=1)
    rows = local_db.get_locations_between(
        device_id,
        start_local.astimezone(timezone.utc).isoformat(),
        end_local.astimezone(timezone.utc).isoformat(),
    )
    points = [{"lat": r["lat"], "lon": r["lon"], "ts": _parse_iso(r["ts"])} for r in rows]
    return day.isoformat(), points


def google_maps_route_url(points: list[dict]) -> str | None:
    """A directions URL with every point as a stop, in order - lets the admin open the whole
    day's route in the actual Google Maps app/site (native pinch-zoom, satellite, street view)
    instead of only the embedded OpenStreetMap preview, which is capped at OSM's own tile zoom."""
    if not points:
        return None
    stops = "/".join(f"{p['lat']},{p['lon']}" for p in points)
    return f"https://www.google.com/maps/dir/{stops}"


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
