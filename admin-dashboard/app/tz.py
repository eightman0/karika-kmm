"""
Single shared display/bucketing timezone - the fleet is Bosnia-only, so every timestamp shown on
the dashboard (and the day/hour buckets analytics charts group events into) converts from the UTC
everything is stored in to this, rather than whatever timezone the server host happens to be in.

Relies on the tzdata PyPI package (see requirements.txt) rather than the system's own tz database,
since python:3.12-slim (the Docker base image this deploys on) does not ship one.
"""

from zoneinfo import ZoneInfo

LOCAL_TZ = ZoneInfo("Europe/Sarajevo")
