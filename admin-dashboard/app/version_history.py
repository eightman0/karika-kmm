from datetime import datetime, timezone

from .firebase import db


def record_publish(
    app: str,
    version_code: str,
    version_name: str,
    apk_url: str,
    apk_sha256: str,
    mandatory: bool,
    published_by: str,
) -> None:
    db().collection("version_history").add(
        {
            "app": app,
            "versionCode": version_code,
            "versionName": version_name,
            "apkUrl": apk_url,
            "apkSha256": apk_sha256,
            "mandatory": mandatory,
            "publishedBy": published_by,
            "publishedAt": datetime.now(timezone.utc),
        }
    )


def get_history(app: str, limit: int = 10) -> list[dict]:
    # Filtered by app here (single equality, no composite index needed) and sorted in Python
    # rather than via .order_by() - combining that with the .where() would need a composite
    # index we can't create from here, and this collection is small enough it doesn't matter.
    docs = db().collection("version_history").where("app", "==", app).stream()
    records = [doc.to_dict() for doc in docs]
    records.sort(
        key=lambda r: r.get("publishedAt") or datetime.min.replace(tzinfo=timezone.utc),
        reverse=True,
    )
    return records[:limit]
