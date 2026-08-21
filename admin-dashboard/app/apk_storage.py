import hashlib

from fastapi import UploadFile

from .firebase import bucket


def upload_apk(apk_file: UploadFile, version_code: str) -> tuple[str, str]:
    """Uploads the APK to Storage and makes it publicly readable - the launcher's
    DownloadManager fetches it with a plain HTTPS GET, no auth support there. Returns
    (public_url, sha256_hex) so the caller can publish both straight to Remote Config."""
    content = apk_file.file.read()
    sha256 = hashlib.sha256(content).hexdigest()

    path = f"salesrep-releases/{version_code}.apk"
    blob = bucket().blob(path)
    blob.upload_from_string(content, content_type="application/vnd.android.package-archive")
    blob.make_public()

    return blob.public_url, sha256
