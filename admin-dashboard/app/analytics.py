"""
Real fleet + usage analytics, computed from local_db - version rollout comes from the devices
table (already tracked for every heartbeat), and the click/screen numbers come from
analytics_events, populated by analytics_ingest.py whenever a device uploads the file its
AnalyticsTracker wrote (see the "Povuci analitiku" button on the devices page).
"""

from collections import Counter
from datetime import datetime, timedelta, timezone

from . import local_db

LINE_DAYS = 15
DONUT_COLORS = ["#9184d9", "#e3c47f", "#e59a9a", "#7fb8e3", "#8fd0a8", "#d99184"]


def get_kpis() -> dict:
    all_devices = local_db.list_devices()
    total_devices = len(all_devices)
    latest = local_db.get_kiosk_version_row() or {}
    latest_code = str(latest.get("version_code") or "")
    on_latest = sum(
        1
        for d in all_devices
        if d["installed_package"] == "karika.distribucija.ba.salesrep"
        and latest_code
        and str(d["installed_version_code"]) == latest_code
    )
    return {
        "total_devices": total_devices,
        "latest_pct": round(on_latest / total_devices * 100) if total_devices else 0,
        "latest_fraction": f"{on_latest} od {total_devices}",
        "devices_with_events": local_db.count_devices_with_events(),
        "events_total": local_db.count_analytics_events(),
        "avg_clicks_per_device": local_db.avg_events_per_device(),
    }


def get_line_chart(width: int = 600, height: int = 160, pad: int = 12) -> dict:
    rows = local_db.events_per_day(LINE_DAYS)
    if not rows:
        return {"empty": True, "width": width, "height": height}

    # Zero-fill the full window (events_per_day only returns days that actually had events) so
    # the x-axis always spans a fixed LINE_DAYS-wide range instead of collapsing to however many
    # days happen to have data - a single active day would otherwise render as one stray point.
    by_day = {r["day"]: r["n"] for r in rows}
    today = datetime.now(timezone.utc).date()
    dates = [(today - timedelta(days=LINE_DAYS - 1 - i)).isoformat() for i in range(LINE_DAYS)]
    values = [by_day.get(d, 0) for d in dates]
    lo, hi = min(values), max(values)
    span = (hi - lo) or 1
    usable_w = width - 2 * pad
    usable_h = height - 2 * pad
    step = usable_w / (len(values) - 1) if len(values) > 1 else 0

    points = []
    point_list = []
    for i, v in enumerate(values):
        x = round(pad + i * step, 1)
        y = round(pad + usable_h - ((v - lo) / span) * usable_h, 1)
        points.append((x, y))
        point_list.append({"x": x, "y": y, "date": dates[i][5:], "value": v})

    return {
        "empty": False,
        "width": width,
        "height": height,
        "dates": [d[5:] for d in dates],
        "values": values,
        "points": " ".join(f"{x},{y}" for x, y in points),
        "point_list": point_list,
        "area_points": f"{pad},{height - pad} " + " ".join(f"{x},{y}" for x, y in points) + f" {width - pad},{height - pad}",
    }


def get_bar_chart(width: int = 600, height: int = 160, pad: int = 12) -> dict:
    rows = {r["hour"]: r["n"] for r in local_db.events_per_hour()}
    hours = [f"{h:02d}" for h in range(24)]
    values = [rows.get(h, 0) for h in hours]
    if not any(values):
        return {"empty": True, "width": width, "height": height}

    hi = max(values) or 1
    usable_w = width - 2 * pad
    usable_h = height - 2 * pad
    gap = 3
    bar_w = (usable_w - gap * (len(values) - 1)) / len(values)

    bars = []
    for i, v in enumerate(values):
        bar_h = (v / hi) * usable_h
        x = pad + i * (bar_w + gap)
        y = pad + usable_h - bar_h
        bars.append({"x": round(x, 1), "y": round(y, 1), "w": round(bar_w, 1), "h": round(bar_h, 1), "hour": hours[i], "value": v})

    return {"empty": False, "width": width, "height": height, "bars": bars}


def get_donut(size: int = 160, stroke: int = 22) -> dict:
    all_devices = local_db.list_devices()
    counts = Counter(
        d["installed_version_name"] or "?"
        for d in all_devices
        if d["installed_package"] == "karika.distribucija.ba.salesrep"
    )
    if not counts:
        return {"empty": True, "size": size}

    radius = (size - stroke) / 2
    circumference = 2 * 3.14159265 * radius
    total = sum(counts.values())

    segments = []
    offset = 0.0
    for i, (version, value) in enumerate(counts.most_common(6)):
        pct = value / total
        dash = pct * circumference
        segments.append(
            {
                "label": version,
                "value": value,
                "pct": round(pct * 100),
                "color": DONUT_COLORS[i % len(DONUT_COLORS)],
                "dasharray": f"{round(dash, 1)} {round(circumference - dash, 1)}",
                "dashoffset": round(-offset, 1),
            }
        )
        offset += dash

    return {"empty": False, "size": size, "radius": radius, "stroke": stroke, "segments": segments, "total": total}


def get_top_screens(limit: int = 8) -> list[dict]:
    return local_db.top_screens(limit)


def get_top_clicks(limit: int = 8) -> list[dict]:
    return local_db.top_clicks(limit)
