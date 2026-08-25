"""
Downloads an uploaded analytics zip from Storage, parses the JSON-lines event files inside (see
AnalyticsTracker.kt on the Android side), and stores each event in local_db - so the /analitika
page shows real usage instead of mockup numbers. Runs synchronously on the analytics-uploaded
callback since these files are small (JSON lines of clicks/screen views, not the app payload
itself).
"""

import io
import json
import logging
import zipfile

from . import local_db
from .firebase import bucket

logger = logging.getLogger(__name__)

_SOURCE_BY_ENTRY = {
    "launcher.jsonl": "launcher",
    "launcher.jsonl.1": "launcher",
    "salesrep.jsonl": "salesrep",
    "salesrep.jsonl.1": "salesrep",
}


def ingest(device_id: str, storage_path: str) -> None:
    try:
        data = bucket().blob(storage_path).download_as_bytes()
    except Exception:
        logger.exception("Could not download analytics zip for %s at %s", device_id, storage_path)
        return

    events = []
    try:
        with zipfile.ZipFile(io.BytesIO(data)) as zf:
            for name in zf.namelist():
                source = _SOURCE_BY_ENTRY.get(name)
                if not source:
                    continue
                for line in zf.read(name).decode("utf-8").splitlines():
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        event = json.loads(line)
                    except ValueError:
                        continue
                    events.append(
                        {
                            "source": source,
                            "ts": event.get("ts"),
                            "user": event.get("user"),
                            "type": event.get("type"),
                            "screen": event.get("screen"),
                            "element": event.get("element"),
                        }
                    )
    except zipfile.BadZipFile:
        logger.exception("Bad analytics zip for %s at %s", device_id, storage_path)
        return

    local_db.insert_analytics_events(device_id, events)
