"""
Builds the Device Owner QR provisioning payload and renders it as a scannable PNG. The fixed
fields (admin component, signature checksum, APK download location) never change; customer_id/
site_id are optional and, when set, go in PROVISIONING_ADMIN_EXTRAS_BUNDLE - a nested JSON object
under that one key, which Android's managed-provisioning QR parser turns into a PersistableBundle
automatically and hands to the app during provisioning (see DeviceMapping.kt /
ProvisioningSuccessActivity.kt on the launcher side).
"""

import json
from pathlib import Path

import qrcode

QR_PATH = Path(__file__).parent / "static" / "provisioning-qr.png"

_FIXED_FIELDS = {
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME":
        "karika.distribucija.ba.launcher/karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM":
        "1r6zVerEdM0pyQzBBDHf_ToS8qliRsL0A_LcfLb2HlE",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION":
        "https://firebasestorage.googleapis.com/v0/b/kiosklauncher-8c837.firebasestorage.app/o/"
        "launcher-releases%2Flauncher-release.apk?alt=media&token=62de3834-b43b-4d38-adaa-4774984878c4",
    "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": True,
}


def build_payload(customer_id: str | None, site_id: str | None) -> dict:
    payload = dict(_FIXED_FIELDS)
    extras = {}
    if customer_id:
        extras["customer_id"] = customer_id
    if site_id:
        extras["site_id"] = site_id
    if extras:
        payload["android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE"] = extras
    return payload


def build_json(customer_id: str | None, site_id: str | None) -> str:
    return json.dumps(build_payload(customer_id, site_id), indent=2)


def generate_qr(customer_id: str | None, site_id: str | None) -> None:
    """Regenerates the QR PNG in place at QR_PATH."""
    payload_json = json.dumps(build_payload(customer_id, site_id))
    img = qrcode.make(payload_json, box_size=8, border=2)
    QR_PATH.parent.mkdir(parents=True, exist_ok=True)
    img.save(QR_PATH)
