import hashlib

from androguard.core.apk import APK
from fastapi import UploadFile
from loguru import logger

from .firebase import bucket

# androguard logs every parsed AXML attribute at DEBUG level via loguru by default - hundreds of
# lines per upload that would drown out anything else in the container's logs.
logger.disable("androguard")


def upload_apk(apk_file: UploadFile) -> tuple[str, str, str, str]:
    """Uploads the APK to Storage and makes it publicly readable - the launcher's
    DownloadManager fetches it with a plain HTTPS GET, no auth support there. version_code and
    version_name are read straight from the APK's own manifest instead of typed by hand in the
    publish form, so what gets published always matches what is actually inside the file (typed
    metadata drifting from the real APK caused a silent update to be skipped once already).
    Returns (public_url, sha256_hex, version_code, version_name)."""
    content = apk_file.file.read()
    sha256 = hashlib.sha256(content).hexdigest()

    apk = APK(content, raw=True)
    version_code = apk.get_androidversion_code()
    version_name = apk.get_androidversion_name()
    if not version_code:
        raise ValueError("Ne mogu pročitati version code iz APK-a - da li je fajl ispravan?")

    path = f"salesrep-releases/{version_code}.apk"
    blob = bucket().blob(path)
    blob.upload_from_string(content, content_type="application/vnd.android.package-archive")
    blob.make_public()

    return blob.public_url, sha256, str(version_code), version_name or ""
