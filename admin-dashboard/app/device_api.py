"""
Plain-HTTPS endpoints the fleet itself calls - no session login, these aren't for a human in a
browser. Replaces what the launcher used to read/write directly in Firestore.
"""

from fastapi import APIRouter
from pydantic import BaseModel

from . import analytics_ingest, local_db
from .launcher_version_config import resolve_launcher_version_for_device
from .version_config import resolve_version_for_device

router = APIRouter(prefix="/api")


@router.get("/version")
def get_version(device_id: str | None = None, app: str = "salesrep"):
    if app == "launcher":
        return resolve_launcher_version_for_device(device_id)
    return resolve_version_for_device(device_id)


class LocationPoint(BaseModel):
    lat: float
    lon: float
    ts: str


class HeartbeatBody(BaseModel):
    installedPackage: str
    installedVersionCode: int
    installedVersionName: str
    androidSdkInt: int
    androidRelease: str
    deviceModel: str
    fcmToken: str | None = None
    maintenanceActive: bool | None = None
    batteryLevel: int | None = None
    batteryCharging: bool | None = None
    locations: list[LocationPoint] = []
    launcherVersionCode: int | None = None
    launcherVersionName: str | None = None


@router.post("/devices/{device_id}/heartbeat")
def post_heartbeat(device_id: str, body: HeartbeatBody):
    local_db.upsert_device_heartbeat(
        device_id,
        body.installedPackage,
        body.installedVersionCode,
        body.installedVersionName,
        body.androidSdkInt,
        body.androidRelease,
        body.deviceModel,
        body.fcmToken,
        body.maintenanceActive,
        body.batteryLevel,
        body.batteryCharging,
        body.launcherVersionCode,
        body.launcherVersionName,
    )
    if body.locations:
        local_db.insert_device_locations(device_id, [p.model_dump() for p in body.locations])
    return {"ok": True}


class LogUploadedBody(BaseModel):
    url: str
    path: str
    requestedAt: str | None = None


@router.post("/devices/{device_id}/log-uploaded")
def post_log_uploaded(device_id: str, body: LogUploadedBody):
    local_db.set_log_uploaded(device_id, body.url, body.path, body.requestedAt)
    return {"ok": True}


class AnalyticsUploadedBody(BaseModel):
    url: str
    path: str


@router.post("/devices/{device_id}/analytics-uploaded")
def post_analytics_uploaded(device_id: str, body: AnalyticsUploadedBody):
    local_db.set_analytics_uploaded(device_id, body.url, body.path)
    analytics_ingest.ingest(device_id, body.path)
    return {"ok": True}


class LoginEventBody(BaseModel):
    email: str
    timestamp: str | None = None


@router.post("/devices/{device_id}/login-event")
def post_login_event(device_id: str, body: LoginEventBody):
    local_db.set_login_event(device_id, body.email, body.timestamp)
    return {"ok": True}


class DeviceMappingBody(BaseModel):
    customerId: str | None = None
    siteId: str | None = None


@router.post("/devices/{device_id}/mapping")
def post_device_mapping(device_id: str, body: DeviceMappingBody):
    local_db.set_device_mapping(device_id, body.customerId, body.siteId)
    return {"ok": True}


class CommandAckBody(BaseModel):
    command: str
    requestId: str | None = None
    status: str
    message: str | None = None


@router.post("/devices/{device_id}/command-ack")
def post_command_ack(device_id: str, body: CommandAckBody):
    local_db.record_command_ack(device_id, body.command, body.requestId, body.status, body.message)
    return {"ok": True}
