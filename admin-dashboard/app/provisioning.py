"""
Builds the Device Owner QR provisioning payload and renders it as a scannable PNG - generated
fresh on every request (see GET /provisioning/qr.png in main.py) rather than cached to disk, so a
payload field change shows up immediately instead of needing someone to notice and regenerate it.
The fixed fields (admin component, signature checksum, APK download location) never change;
everything else comes from the saved provisioning extras (see local_db.get_provisioning_extras()):
customer_id/site_id go in PROVISIONING_ADMIN_EXTRAS_BUNDLE - a nested JSON object under that one
key, which Android's managed-provisioning QR parser turns into a PersistableBundle automatically
and hands to the app during provisioning (see DeviceMapping.kt / ProvisioningSuccessActivity.kt on
the launcher side); wifi_* go in Android's own top-level PROVISIONING_WIFI_* extras, which managed
provisioning uses to join the network itself before it ever needs one (to download the launcher
APK) - without this, whoever's provisioning a device has to join it to wifi by hand first.
"""

import io
import json

import qrcode

from . import launcher_version_config

# Bootstrap-only fallback, for the (normally never-hit) case where no launcher version has ever
# been published yet - resolve_apk_download_url() below prefers whatever IS currently published
# instead, specifically so this never has to be kept in sync by hand again. That manual-sync gap
# is exactly what left a real device re-enrolling onto a stale launcher build after every factory
# reset, since QR provisioning previously had nothing to do with what was actually published.
DEFAULT_APK_DOWNLOAD_URL = (
    "https://firebasestorage.googleapis.com/v0/b/kiosklauncher-8c837.firebasestorage.app/o/"
    "launcher-releases%2Flauncher-release.apk?alt=media&token=62de3834-b43b-4d38-adaa-4774984878c4"
)

_FIXED_FIELDS = {
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME":
        "karika.distribucija.ba.launcher/karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver",
    "android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM":
        "1r6zVerEdM0pyQzBBDHf_ToS8qliRsL0A_LcfLb2HlE",
    "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED": False,
}


def resolve_apk_download_url(extras: dict) -> str:
    """An explicit override (someone typed a URL in on the provisioning page) always wins - useful
    for provisioning a one-off build without promoting it as the fleet's current version. Absent
    that, follow whatever is currently published/activated for the launcher (same version
    "Ažuriraj launcher"/self-update already sends everyone else), so provisioning can never drift
    from it again. Only falls back to the hardcoded bootstrap URL if nothing has ever been
    published at all."""
    override = extras.get("apk_download_url")
    if override:
        return override
    published_url = launcher_version_config.get_launcher_version()["apk_url"]
    return published_url or DEFAULT_APK_DOWNLOAD_URL


def build_payload(extras: dict | None) -> dict:
    extras = extras or {}
    payload = dict(_FIXED_FIELDS)
    payload["android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION"] = (
        resolve_apk_download_url(extras)
    )

    admin_extras = {}
    if extras.get("customer_id"):
        admin_extras["customer_id"] = extras["customer_id"]
    if extras.get("site_id"):
        admin_extras["site_id"] = extras["site_id"]
    if admin_extras:
        payload["android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE"] = admin_extras

    ssid = extras.get("wifi_ssid")
    if ssid:
        payload["android.app.extra.PROVISIONING_WIFI_SSID"] = ssid
        security_type = extras.get("wifi_security_type") or "NONE"
        payload["android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE"] = security_type
        if security_type != "NONE" and extras.get("wifi_password"):
            payload["android.app.extra.PROVISIONING_WIFI_PASSWORD"] = extras["wifi_password"]

    return payload


def build_json(extras: dict | None) -> str:
    return json.dumps(build_payload(extras), indent=2)


def qr_png_bytes(extras: dict | None) -> bytes:
    payload_json = json.dumps(build_payload(extras))
    img = qrcode.make(payload_json, box_size=8, border=2)
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return buf.getvalue()
