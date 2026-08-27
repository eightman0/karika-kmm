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
from collections import Counter
from contextlib import contextmanager
from datetime import datetime, timedelta, timezone
from pathlib import Path

from .tz import LOCAL_TZ

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


def _ensure_columns(conn: sqlite3.Connection, table: str, columns: dict[str, str]) -> None:
    """Adds columns introduced after a device's DB file was first created - CREATE TABLE IF NOT
    EXISTS only runs its body on a brand new file, so an existing devices.db needs its own
    migration path for each new column."""
    existing = {row["name"] for row in conn.execute(f"PRAGMA table_info({table})")}
    for name, col_type in columns.items():
        if name not in existing:
            conn.execute(f"ALTER TABLE {table} ADD COLUMN {name} {col_type}")


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
                last_log_upload_request_handled_at TEXT,
                fcm_token TEXT,
                last_analytics_upload_url TEXT,
                last_analytics_upload_path TEXT,
                last_analytics_upload_at TEXT,
                customer_id TEXT,
                site_id TEXT,
                last_login_email TEXT,
                last_login_at TEXT
            )
            """
        )
        _ensure_columns(
            conn,
            "devices",
            {
                "fcm_token": "TEXT",
                "last_analytics_upload_url": "TEXT",
                "last_analytics_upload_path": "TEXT",
                "last_analytics_upload_at": "TEXT",
                "customer_id": "TEXT",
                "site_id": "TEXT",
                "last_login_email": "TEXT",
                "last_login_at": "TEXT",
                "maintenance_active": "INTEGER",
                "kiosk_exit_active": "INTEGER",
            },
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS command_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id TEXT,
                command TEXT,
                request_id TEXT,
                status TEXT,
                message TEXT,
                created_at TEXT
            )
            """
        )
        conn.execute(
            "CREATE INDEX IF NOT EXISTS idx_command_log_device ON command_log(device_id, created_at)"
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
            CREATE TABLE IF NOT EXISTS staged_kiosk_version (
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
            CREATE TABLE IF NOT EXISTS staged_version_targets (
                device_id TEXT PRIMARY KEY
            )
            """
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS provisioning_extras (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                customer_id TEXT,
                site_id TEXT
            )
            """
        )
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS analytics_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id TEXT,
                source TEXT,
                ts TEXT,
                user TEXT,
                type TEXT,
                screen TEXT,
                element TEXT
            )
            """
        )
        conn.execute(
            "CREATE INDEX IF NOT EXISTS idx_analytics_events_ts ON analytics_events(ts)"
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
    fcm_token: str | None,
    maintenance_active: bool | None = None,
    kiosk_exit_active: bool | None = None,
) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (
                id, installed_package, installed_version_code, installed_version_name,
                android_sdk_int, android_release, device_model, fcm_token, last_seen_at,
                maintenance_active, kiosk_exit_active
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                installed_package=excluded.installed_package,
                installed_version_code=excluded.installed_version_code,
                installed_version_name=excluded.installed_version_name,
                android_sdk_int=excluded.android_sdk_int,
                android_release=excluded.android_release,
                device_model=excluded.device_model,
                fcm_token=COALESCE(excluded.fcm_token, devices.fcm_token),
                last_seen_at=excluded.last_seen_at,
                maintenance_active=excluded.maintenance_active,
                kiosk_exit_active=excluded.kiosk_exit_active
            """,
            (
                device_id, installed_package, installed_version_code, installed_version_name,
                android_sdk_int, android_release, device_model, fcm_token, now_iso(),
                int(maintenance_active) if maintenance_active is not None else None,
                int(kiosk_exit_active) if kiosk_exit_active is not None else None,
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


def set_analytics_uploaded(device_id: str, url: str, path: str) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (id, last_analytics_upload_url, last_analytics_upload_path,
                last_analytics_upload_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                last_analytics_upload_url=excluded.last_analytics_upload_url,
                last_analytics_upload_path=excluded.last_analytics_upload_path,
                last_analytics_upload_at=excluded.last_analytics_upload_at
            """,
            (device_id, url, path, now_iso()),
        )


def set_login_event(device_id: str, email: str, at: str | None) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (id, last_login_email, last_login_at)
            VALUES (?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                last_login_email=excluded.last_login_email,
                last_login_at=excluded.last_login_at
            """,
            (device_id, email, at or now_iso()),
        )


def set_device_mapping(device_id: str, customer_id: str | None, site_id: str | None) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO devices (id, customer_id, site_id) VALUES (?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                customer_id=excluded.customer_id,
                site_id=excluded.site_id
            """,
            (device_id, customer_id, site_id),
        )


def record_command_ack(device_id: str, command: str, request_id: str | None, status: str, message: str | None) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO command_log (device_id, command, request_id, status, message, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (device_id, command, request_id, status, message, now_iso()),
        )


def get_command_log(device_id: str, limit: int = 20) -> list[dict]:
    with _connect() as conn:
        rows = conn.execute(
            """
            SELECT * FROM command_log WHERE device_id = ? ORDER BY created_at DESC LIMIT ?
            """,
            (device_id, limit),
        ).fetchall()
        return [dict(row) for row in rows]


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


# --- analytics events -----------------------------------------------------------

def insert_analytics_events(device_id: str, events: list[dict]) -> None:
    if not events:
        return
    with _connect() as conn:
        conn.executemany(
            """
            INSERT INTO analytics_events (device_id, source, ts, user, type, screen, element)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
            [
                (
                    device_id, e.get("source"), e.get("ts"), e.get("user"),
                    e.get("type"), e.get("screen"), e.get("element"),
                )
                for e in events
            ],
        )


def count_analytics_events() -> int:
    with _connect() as conn:
        return conn.execute("SELECT COUNT(*) FROM analytics_events").fetchone()[0]


def count_devices_with_events() -> int:
    with _connect() as conn:
        return conn.execute("SELECT COUNT(DISTINCT device_id) FROM analytics_events").fetchone()[0]


def avg_events_per_device() -> float:
    with _connect() as conn:
        row = conn.execute(
            "SELECT COUNT(*) AS n, COUNT(DISTINCT device_id) AS d FROM analytics_events WHERE type = 'click'"
        ).fetchone()
    return round(row["n"] / row["d"], 1) if row["d"] else 0.0


def events_per_day(days: int) -> list[dict]:
    # SQLite's strftime() only knows UTC (or a fixed hour offset, wrong half the year under DST),
    # so bucketing by Sarajevo calendar day has to happen in Python instead of in the query.
    cutoff = (datetime.now(timezone.utc) - timedelta(days=days)).isoformat()
    with _connect() as conn:
        rows = conn.execute("SELECT ts FROM analytics_events WHERE ts >= ?", (cutoff,)).fetchall()
    counts = Counter(
        datetime.fromisoformat(row["ts"]).astimezone(LOCAL_TZ).strftime("%Y-%m-%d") for row in rows
    )
    return [{"day": day, "n": n} for day, n in sorted(counts.items())]


def events_per_hour() -> list[dict]:
    with _connect() as conn:
        rows = conn.execute("SELECT ts FROM analytics_events").fetchall()
    counts = Counter(
        datetime.fromisoformat(row["ts"]).astimezone(LOCAL_TZ).strftime("%H") for row in rows
    )
    return [{"hour": hour, "n": n} for hour, n in sorted(counts.items())]


def top_screens(limit: int = 8) -> list[dict]:
    with _connect() as conn:
        rows = conn.execute(
            """
            SELECT screen, COUNT(*) AS n FROM analytics_events
            WHERE type = 'screen' GROUP BY screen ORDER BY n DESC LIMIT ?
            """,
            (limit,),
        ).fetchall()
        return [dict(row) for row in rows]


def top_clicks(limit: int = 8) -> list[dict]:
    with _connect() as conn:
        rows = conn.execute(
            """
            SELECT screen, element, COUNT(*) AS n FROM analytics_events
            WHERE type = 'click' GROUP BY screen, element ORDER BY n DESC LIMIT ?
            """,
            (limit,),
        ).fetchall()
        return [dict(row) for row in rows]


# --- provisioning extras --------------------------------------------------------

def get_provisioning_extras() -> dict | None:
    with _connect() as conn:
        row = conn.execute("SELECT * FROM provisioning_extras WHERE id = 1").fetchone()
        return dict(row) if row else None


def set_provisioning_extras(customer_id: str | None, site_id: str | None) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO provisioning_extras (id, customer_id, site_id) VALUES (1, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                customer_id=excluded.customer_id,
                site_id=excluded.site_id
            """,
            (customer_id, site_id),
        )


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


# --- staged kiosk version (published but not yet sent to everyone) -------------

def get_staged_kiosk_version_row() -> dict | None:
    with _connect() as conn:
        row = conn.execute("SELECT * FROM staged_kiosk_version WHERE id = 1").fetchone()
        return dict(row) if row else None


def set_staged_kiosk_version(
    version_code: int, version_name: str, apk_url: str, apk_sha256: str, mandatory: bool
) -> None:
    with _connect() as conn:
        conn.execute(
            """
            INSERT INTO staged_kiosk_version (id, version_code, version_name, apk_url, apk_sha256, mandatory)
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
        conn.execute("DELETE FROM staged_version_targets")


def clear_staged_kiosk_version() -> None:
    with _connect() as conn:
        conn.execute("DELETE FROM staged_kiosk_version WHERE id = 1")
        conn.execute("DELETE FROM staged_version_targets")


def add_staged_target(device_id: str) -> None:
    with _connect() as conn:
        conn.execute(
            "INSERT INTO staged_version_targets (device_id) VALUES (?) ON CONFLICT(device_id) DO NOTHING",
            (device_id,),
        )


def is_staged_target(device_id: str) -> bool:
    with _connect() as conn:
        row = conn.execute(
            "SELECT 1 FROM staged_version_targets WHERE device_id = ?", (device_id,)
        ).fetchone()
        return row is not None


def count_staged_targets() -> int:
    with _connect() as conn:
        return conn.execute("SELECT COUNT(*) FROM staged_version_targets").fetchone()[0]


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
