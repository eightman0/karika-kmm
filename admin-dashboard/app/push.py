"""
FCM sends - untouched by the Firestore-to-SQLite migration, since push was never gRPC/Firestore
based to begin with. Two channels: a broadcast topic every device subscribes to (for "check for
an update now"), and a per-device topic keyed by the device's own Android ID (for "upload your
logs now" - this needs to reach exactly one device, not the whole fleet).
"""

import logging

from firebase_admin import messaging

from .firebase import init_messaging

logger = logging.getLogger(__name__)

BROADCAST_TOPIC = "kiosk-updates"


def _device_topic(device_id: str) -> str:
    return f"device_{device_id}"


def send_version_check(version_code: str) -> None:
    try:
        init_messaging()
        messaging.send(
            messaging.Message(
                topic=BROADCAST_TOPIC,
                data={"type": "version_check", "versionCode": str(version_code)},
            )
        )
    except Exception:
        # The periodic on-device check picks this up within ~30 min regardless, so a push
        # failure (e.g. a transient FCM hiccup) shouldn't block the publish itself.
        logger.exception("Failed to send FCM update-check ping")


def send_log_request(device_id: str, requested_at: str) -> None:
    try:
        init_messaging()
        messaging.send(
            messaging.Message(
                topic=_device_topic(device_id),
                data={"type": "log_request", "requestedAt": requested_at},
            )
        )
    except Exception:
        logger.exception("Failed to send FCM log-request ping for %s", device_id)


def send_location_request(device_id: str) -> None:
    try:
        init_messaging()
        messaging.send(
            messaging.Message(topic=_device_topic(device_id), data={"type": "location_request"})
        )
    except Exception:
        logger.exception("Failed to send FCM location-request ping for %s", device_id)


def send_location_request_all() -> None:
    try:
        init_messaging()
        messaging.send(
            messaging.Message(topic=BROADCAST_TOPIC, data={"type": "location_request"})
        )
    except Exception:
        logger.exception("Failed to send FCM location-request broadcast")
