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


def send_version_check_to_device(fcm_token: str, version_code: str) -> str:
    """Nudges one specific device to check for an update right now. Direct token, not its own
    topic - unlike the fleet broadcast below, this is triggered by an admin explicitly picking a
    device and a version and expecting it to actually happen, not a background nudge that can
    shrug off a delivery hiccup. (Topic delivery was tried first and turned out to be unreliable
    in practice - direct-token commands like maintenance/ping/reboot always got through, this one
    silently didn't.) The periodic ~30 min poll still picks up the staged version regardless if
    this send is ever missed."""
    return send_command_to_token(fcm_token, "version_check", {"versionCode": version_code})


def send_version_check_all(version_code: str) -> None:
    """Used only once a staged build is promoted to stable ("Posalji svima") - every device would
    reach the same build on its next periodic poll regardless, this just makes it immediate."""
    try:
        init_messaging()
        messaging.send(
            messaging.Message(
                topic=BROADCAST_TOPIC,
                data={"command": "version_check", "versionCode": str(version_code)},
            )
        )
    except Exception:
        logger.exception("Failed to send FCM update-check broadcast")


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


def send_maintenance(fcm_token: str, enable: bool) -> str:
    return send_command_to_token(fcm_token, "maintenance_on" if enable else "maintenance_off")


def send_open_settings(fcm_token: str) -> str:
    return send_command_to_token(fcm_token, "open_settings")


def send_ping(fcm_token: str) -> str:
    return send_command_to_token(fcm_token, "ping")

