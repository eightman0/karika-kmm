import hashlib
import uuid
from urllib.parse import quote

from androguard.core.apk import APK
from fastapi import UploadFile
from loguru import logger

from .firebase import bucket

# androguard logs every parsed AXML attribute at DEBUG level via loguru by default - hundreds of
# lines per upload that would drown out anything else in the container's logs.
logger.disable("androguard")

_APP_PACKAGE_NAMES = {
    "salesrep": "karika.distribucija.ba.salesrep",
    "launcher": "karika.distribucija.ba.launcher",
}


def upload_apk(apk_file: UploadFile, app: str = "salesrep") -> tuple[str, str, str, str]:
    """Uploads the APK to Storage and returns a Firebase-style download URL (?alt=media&token=...)
    - both salesrep's DownloadManager and the launcher's own self-update fetch it with a plain
    HTTPS GET, no auth support there. Deliberately not blob.make_public() + blob.public_url: that
    depends on this project's Storage security rules allowing public reads for the given path,
    which turned out to only be configured for salesrep-releases/** - launcher-releases/** APKs
    silently 403'd on real devices despite make_public() reporting success, since GCS ACLs and
    Firebase Storage security rules are separate systems and rules win. Setting a per-object
    download token bypasses security rules entirely (the same mechanism the provisioning page's
    original hardcoded APK URL already relied on), so it does not matter whether a rule exists for
    this path - works uniformly for every app, no rules changes needed.

    version_code and version_name are read straight from the APK's own manifest instead of typed
    by hand in the publish form, so what gets published always matches what is actually inside the
    file (typed metadata drifting from the real APK caused a silent update to be skipped once
    already). The package name is checked too, so an APK for the wrong app can't get uploaded
    under the wrong tab by mistake. Returns (download_url, sha256_hex, version_code, version_name)."""
    content = apk_file.file.read()
    sha256 = hashlib.sha256(content).hexdigest()

    apk = APK(content, raw=True)
    version_code = apk.get_androidversion_code()
    version_name = apk.get_androidversion_name()
    if not version_code:
        raise ValueError("Ne mogu pročitati version code iz APK-a - da li je fajl ispravan?")

    expected_package = _APP_PACKAGE_NAMES.get(app)
    actual_package = apk.get_package()
    if expected_package and actual_package != expected_package:
        raise ValueError(
            f"Ovaj APK je za paket '{actual_package}', a očekivan je '{expected_package}' za {app}"
        )

    path = f"{app}-releases/{version_code}.apk"
    bucket_ref = bucket()
    blob = bucket_ref.blob(path)
    blob.upload_from_string(content, content_type="application/vnd.android.package-archive")

    token = str(uuid.uuid4())
    blob.metadata = {"firebaseStorageDownloadTokens": token}
    blob.patch()
    download_url = (
        f"https://firebasestorage.googleapis.com/v0/b/{bucket_ref.name}/o/"
        f"{quote(path, safe='')}?alt=media&token={token}"
    )

    return download_url, sha256, str(version_code), version_name or ""
