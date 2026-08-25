"""
Illustrative fleet analytics. There's no real event pipeline yet (the app only ever wanted
device heartbeats + version history), so these are representative mockup numbers matching the
Karika Ops design - enough to make the /analitika page useful to look at until real usage
tracking exists. Replace with real local_db-backed aggregates once that's built.
"""

LINE_DATES = [
    "07.08.", "08.08.", "09.08.", "10.08.", "11.08.", "12.08.", "13.08.",
    "14.08.", "15.08.", "16.08.", "17.08.", "18.08.", "19.08.", "20.08.", "21.08.",
]
LINE_VALUES = [31, 33, 30, 35, 38, 40, 37, 39, 42, 44, 43, 46, 45, 48, 41]

BAR_HOURS = ["06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"]
BAR_VALUES = [4, 9, 22, 35, 40, 38, 44, 47, 43, 39, 36, 30, 21, 12, 6]

DONUT_SEGMENTS = [
    {"label": "1.4.2 (142)", "value": 45, "color": "#9184d9"},
    {"label": "1.4.1 (141)", "value": 5, "color": "#e3c47f"},
    {"label": "1.4.0 i starije", "value": 2, "color": "#e59a9a"},
]


def get_kpis() -> dict:
    return {
        "total_devices": 52,
        "latest_pct": 87,
        "latest_fraction": "45 od 52",
        "latest_delta": "+12pp",
        "avg_daily_active": 41,
        "sessions_per_device": 6.4,
        "sessions_delta": "-0.3 vs prošla sedmica",
    }


def get_line_chart(width: int = 600, height: int = 160, pad: int = 12) -> dict:
    values = LINE_VALUES
    lo, hi = min(values), max(values)
    span = (hi - lo) or 1
    usable_w = width - 2 * pad
    usable_h = height - 2 * pad
    step = usable_w / (len(values) - 1)

    points = []
    point_list = []
    for i, v in enumerate(values):
        x = round(pad + i * step, 1)
        y = round(pad + usable_h - ((v - lo) / span) * usable_h, 1)
        points.append((x, y))
        point_list.append({"x": x, "y": y, "date": LINE_DATES[i], "value": v})

    return {
        "width": width,
        "height": height,
        "dates": LINE_DATES,
        "values": values,
        "points": " ".join(f"{x},{y}" for x, y in points),
        "point_list": point_list,
        "last_point": points[-1],
        "first_point": points[0],
        "area_points": f"{pad},{height - pad} " + " ".join(f"{x},{y}" for x, y in points) + f" {width - pad},{height - pad}",
    }


def get_bar_chart(width: int = 600, height: int = 160, pad: int = 12) -> dict:
    values = BAR_VALUES
    hi = max(values) or 1
    usable_w = width - 2 * pad
    usable_h = height - 2 * pad
    gap = 6
    bar_w = (usable_w - gap * (len(values) - 1)) / len(values)

    bars = []
    for i, v in enumerate(values):
        bar_h = (v / hi) * usable_h
        x = pad + i * (bar_w + gap)
        y = pad + usable_h - bar_h
        bars.append({"x": round(x, 1), "y": round(y, 1), "w": round(bar_w, 1), "h": round(bar_h, 1), "hour": BAR_HOURS[i], "value": v})

    return {"width": width, "height": height, "bars": bars}


def get_donut(size: int = 160, stroke: int = 22) -> dict:
    radius = (size - stroke) / 2
    circumference = 2 * 3.14159265 * radius
    total = sum(s["value"] for s in DONUT_SEGMENTS) or 1

    segments = []
    offset = 0.0
    for seg in DONUT_SEGMENTS:
        pct = seg["value"] / total
        dash = pct * circumference
        segments.append(
            {
                "label": seg["label"],
                "value": seg["value"],
                "pct": round(pct * 100),
                "color": seg["color"],
                "dasharray": f"{round(dash, 1)} {round(circumference - dash, 1)}",
                "dashoffset": round(-offset, 1),
            }
        )
        offset += dash

    return {"size": size, "radius": radius, "stroke": stroke, "segments": segments, "total": total}
