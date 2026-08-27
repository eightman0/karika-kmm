"""
FCM sends - untouched by the Firestore-to-SQLite migration, since push was never gRPC/Firestore
based to begin with. Two targeting modes: topic (broadcast to everyone, or a device's own topic -
used when there's a periodic fallback so a delivery hiccup isn't fatal) and direct token (for
commands where the caller wants to know the send itself succeeded right away, or where there's no
fallback if it's missed - destructive actions in particular).

Every command carries a requestId so the launcher can ack it back via
/api/devices/{id}/command-ack and it's traceable end to end (see local_db.command_log).
"""

import logging
import uuid

from firebase_admin import messaging

from .firebase import init_messaging

logger = logging.getLogger(__name__)

BROADCAST_TOPIC = "kiosk-updates"


def _device_topic(device_id: str) -> str:
    return f"device_{device_id}"


def new_request_id() -> str:
    return str(uuid.uuid4())


def send_version_check(version_code: str) -> None:
    try:
        init_messaging()
        messaging.send(
            messaging.Message(
                topic=BROADCAST_TOPIC,
                data={"command": "version_check", "versionCode": str(version_code)},
            )
        )
    except Exception:
        # The periodic on-device check picks this up within ~30 min regardless, so a push
        # failure (e.g. a transient FCM hiccup) shouldn't block the publish itself.
        logger.exception("Failed to send FCM update-check ping")


def send_analytics_request_all() -> None:
    # Fleet-wide, same channel version-check nudges already use - every device that's on and
    # connected uploads its own local analytics events independently, no per-device state needed
    # for the request itself.
    try:
        init_messaging()
        messaging.send(
            messaging.Message(topic=BROADCAST_TOPIC, data={"command": "analytics_request"})
        )
    except Exception:
        logger.exception("Failed to send FCM analytics-request broadcast")


def send_log_request(device_id: str, requested_at: str) -> str:
    request_id = new_request_id()
    try:
        init_messaging()
        messaging.send(
            messaging.Message(
                topic=_device_topic(device_id),
                data={"command": "log_request", "requestId": request_id, "requestedAt": requested_at},
            )
        )
    except Exception:
        logger.exception("Failed to send FCM log-request ping for %s", device_id)
    return request_id


def send_command_to_token(fcm_token: str, command: str, extra: dict | None = None) -> str:
    """Direct-to-device send for commands with no periodic fallback - the caller finds out
    immediately if the send itself failed, and gets a requestId back to track the ack."""
    request_id = new_request_id()
    init_messaging()
    data = {"command": command, "requestId": request_id}
    if extra:
        data.update({k: str(v) for k, v in extra.items()})
    messaging.send(messaging.Message(token=fcm_token, data=data))
    return request_id


def send_factory_reset(fcm_token: str) -> str:
    return send_command_to_token(fcm_token, "factory_reset")


def send_reboot(fcm_token: str) -> str:
    return send_command_to_token(fcm_token, "reboot")


def send_exit_kiosk(fcm_token: str) -> str:
    return send_command_to_token(fcm_token, "exit_kiosk")


def send_enter_kiosk(fcm_token: str) -> str:
    return send_command_to_token(fcm_token, "enter_kiosk")


def send_maintenance(fcm_token: str, enable: bool) -> str:
    return send_command_to_token(fcm_token, "maintenance_on" if enable else "maintenance_off")


