"""
Local SQLite store for device fleet state and published version info - replaces Firestore for
these two things. Firestore's client is gRPC-only on both the Python and Android side (no REST
fallback exists for either), and gRPC turned out to be what a bad network moment blocks while
plain HTTPS keeps working - so this data now lives right next to the dashboard instead of round-
tripping through a Google backend the fleet has no control over. Firebase Storage (APK hosting,
log files) and FCM (push) are untouched - both are plain-HTTPS-based already, not gRPC, and were
never actually part of the problem.
"""

import os
import sqlite3
from contextlib import contextmanager
from datetime import datetime, timezone
from pathlib import Path

DB_PATH = Path(os.environ.get("LOCAL_DB_PATH", "data/karika.db"))


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


@contextmanager
def _connect():
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(DB_PATH, timeout=10)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    try:
        yield conn
        conn.commit()
    finally:
        conn.close()


def init_db() -> None:
    with _connect() as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS devices (
                id TEXT PRIMARY KEY,
                installed_package TEXT,
                installed_version_code INTEGER,
                installed_version_name TEXT,
                android_sdk_int INTEGER,
                android_release TEXT,
                device_model TEXT,
                last_seen_at TEXT,
                log_requested_at TEXT,
                last_log_upload_url TEXT,
                last_log_upload_path TEXT,
                last_log_upload_at TEXT,
                last_log_upload_request_handled_at TEXT
            )
            """
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS kiosk_version (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                version_code INTEGER,
                version_name TEXT,
                apk_url TEXT,
                apk_sha256 TEXT,
                mandatory INTEGER
            )
            """
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS version_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                app TEXT,
                version_code INTEGER,
                version_name TEXT,
                apk_url TEXT,
                apk_sha256 TEXT,
                mandatory INTEGER,
                published_by TEXT,
                published_at TEXT
            )
            """
        )


# --- devices -----------------------------------------------------------------

def upsert_device_heartbeat(
    device_id: str,
    installed_package: str,
    installed_version_code: int,
    installed_version_name: str,
    android_sdk_int: int,
    android_release: str,
    device_model: str,
) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (
                id, installed_package, installed_version_code, installed_version_name,
                android_sdk_int, android_release, device_model, last_seen_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                installed_package=excluded.installed_package,
                installed_version_code=excluded.installed_version_code,
                installed_version_name=excluded.installed_version_name,
                android_sdk_int=excluded.android_sdk_int,
                android_release=excluded.android_release,
                device_model=excluded.device_model,
                last_seen_at=excluded.last_seen_at
            """,
            (
                device_id, installed_package, installed_version_code, installed_version_name,
                android_sdk_int, android_release, device_model, now_iso(),
            ),
        )


def set_log_uploaded(device_id: str, url: str, path: str, requested_at: str | None) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (id, last_log_upload_url, last_log_upload_path,
                last_log_upload_at, last_log_upload_request_handled_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                last_log_upload_url=excluded.last_log_upload_url,
                last_log_upload_path=excluded.last_log_upload_path,
                last_log_upload_at=excluded.last_log_upload_at,
                last_log_upload_request_handled_at=excluded.last_log_upload_request_handled_at
            """,
            (device_id, url, path, now_iso(), requested_at),
        )


def request_log_pull(device_id: str) -> str:
    requested_at = now_iso()
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (id, log_requested_at) VALUES (?, ?)
            ON CONFLICT(id) DO UPDATE SET log_requested_at=excluded.log_requested_at
            """,
            (device_id, requested_at),
        )
    return requested_at


def list_devices() -> list[dict]:
    with _connect() as conn:
        rows = conn.execute("SELECT * FROM devices").fetchall()
        return [dict(row) for row in rows]


def get_device(device_id: str) -> dict | None:
    with _connect() as conn:
        row = conn.execute("SELECT * FROM devices WHERE id = ?", (device_id,)).fetchone()
        return dict(row) if row else None


def delete_device(device_id: str) -> None:
    with _connect() as conn:
        conn.execute("DELETE FROM devices WHERE id = ?", (device_id,))


# --- kiosk version -------------------------------------------------------------

def get_kiosk_version_row() -> dict | None:
    with _connect() as conn:
        row = conn.execute("SELECT * FROM kiosk_version WHERE id = 1").fetchone()
        return dict(row) if row else None


def set_kiosk_version(
    version_code: int, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool
) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO kiosk_version (id, version_code, version_name, apk_url, apk_sha256, mandatory)
            VALUES (1, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                version_code=excluded.version_code,
                version_name=excluded.version_name,
                apk_url=excluded.apk_url,
                apk_sha256=excluded.apk_sha256,
                mandatory=excluded.mandatory
            """,
            (version_code, version_name, apk_url, apk_sha256, int(mandatory)),
        )


# --- version history -----------------------------------------------------------

def record_publish(
    app: str, version_code: int, version_name: str, apk_url: str, apk_sha256: str,
    mandatory: bool, published_by: str,
) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO version_history
                (app, version_code, version_name, apk_url, apk_sha256, mandatory, published_by, published_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (app, version_code, version_name, apk_url, apk_sha256, int(mandatory), published_by, now_iso()),
        )


def get_history(app: str, limit: int = 10) -> list[dict]:
    with _connect() as conn:
        rows = conn.execute(
            "SELECT * FROM version_history WHERE app = ? ORDER BY published_at DESC LIMIT ?",
            (app, limit),
        ).fetchall()
        return [dict(row) for row in rows]
