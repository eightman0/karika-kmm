"""
Plain-HTTPS endpoints the fleet itself calls - no session login, these aren't for a human in a
browser. Replaces what the launcher used to read/write directly in Firestore.
"""

from fastapi import APIRouter
from pydantic import BaseModel

from . import local_db
from .version_config import get_kiosk_version

router = APIRouter(prefix="/api")


@router.get("/version")
def get_version():
    return get_kiosk_version()


class HeartbeatBody(BaseModel):
    installedPackage: str
    installedVersionCode: int
    installedVersionName: str
    androidSdkInt: int
    androidRelease: str
    deviceModel: str


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
    )
    return {"ok": True}


class LogUploadedBody(BaseModel):
    url: str
    path: str
    requestedAt: str | None = None


@router.post("/devices/{device_id}/log-uploaded")
def post_log_uploaded(device_id: str, body: LogUploadedBody):
    local_db.set_log_uploaded(device_id, body.url, body.path, body.requestedAt)
    return {"ok": True}


class LocationBody(BaseModel):
    latitude: float
    longitude: float
    accuracy: float


@router.post("/devices/{device_id}/location")
def post_location(device_id: str, body: LocationBody):
    local_db.set_device_location(device_id, body.latitude, body.longitude, body.accuracy)
    return {"ok": True}
