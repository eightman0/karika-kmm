from urllib.parse import quote

from fastapi import Depends, FastAPI, File, Form, Request, UploadFile
from fastapi.responses import RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware

from . import analytics, apk_storage, auth, devices, version_config, version_history

app = FastAPI(title="Karika Kiosk Admin")
app.add_middleware(SessionMiddleware, secret_key=auth.SESSION_SECRET, same_site="lax")
app.mount("/static", StaticFiles(directory="app/static"), name="static")
templates = Jinja2Templates(directory="app/templates")

require_login = Depends(auth.require_login)

APP = "salesrep"  # the only app published through this dashboard - launcher is Device Owner,
# changes rarely, and is deliberately not self-updated this way


def _list_devices_safe() -> tuple[list[dict], bool]:
    """A Firestore hiccup shouldn't take down a whole page with a raw 500 - every route that
    needs the device list falls back to an empty one and shows a banner instead."""
    try:
        return devices.list_devices(), False
    except Exception:
        return [], True


@app.get("/")
def root():
    return RedirectResponse("/devices")


@app.get("/login")
def login_page(request: Request, error: str | None = None):
    try:
        device_count = len(devices.list_devices())
    except Exception:
        # Firestore hiccup shouldn't take down the login page - it's just a footer stat.
        device_count = None
    return templates.TemplateResponse(
        request, "login.html", {"error": error, "active_page": "login", "device_count": device_count}
    )


@app.post("/login")
def login_submit(request: Request, username: str = Form(...), password: str = Form(...)):
    if not auth.check_credentials(username, password):
        return RedirectResponse(
            f"/login?error={quote('Pogrešno korisničko ime ili lozinka')}", status_code=303
        )
    request.session["logged_in"] = True
    request.session["username"] = username
    return RedirectResponse("/devices", status_code=303)


@app.get("/logout")
def logout(request: Request):
    request.session.clear()
    return RedirectResponse("/login", status_code=303)


@app.get("/devices", dependencies=[require_login])
def devices_page(request: Request, q: str = "", app: str = "all"):
    all_devices, firestore_error = _list_devices_safe()
    filtered = devices.filter_devices(all_devices, q, app)
    try:
        latest_salesrep_code = version_config.get_kiosk_version()["version_code"]
    except Exception:
        latest_salesrep_code = None
    return templates.TemplateResponse(
        request,
        "devices.html",
        {
            "devices": filtered,
            "summary": devices.fleet_summary(all_devices),
            "q": q,
            "app_filter": app,
            "latest_salesrep_code": latest_salesrep_code,
            "firestore_error": firestore_error,
            "active_page": "devices",
        },
    )


@app.get("/devices/{device_id}", dependencies=[require_login])
def device_detail_page(request: Request, device_id: str):
    device = devices.get_device(device_id)
    if device is None:
        return RedirectResponse("/devices")
    return templates.TemplateResponse(
        request, "device_detail.html", {"device": device, "active_page": "devices"}
    )


@app.post("/devices/{device_id}/request-logs", dependencies=[require_login])
def request_logs(device_id: str):
    devices.request_logs(device_id)
    return RedirectResponse(f"/devices/{device_id}", status_code=303)


@app.get("/devices/{device_id}/log", dependencies=[require_login])
def download_log(device_id: str):
    device = devices.get_device(device_id)
    path = device.get("lastLogUploadPath") if device else None
    if not path:
        return RedirectResponse(f"/devices/{device_id}")
    return RedirectResponse(devices.signed_log_url(path))


@app.get("/versions", dependencies=[require_login])
def versions_page(request: Request, error: str | None = None, published: str | None = None):
    all_devices, firestore_error = _list_devices_safe()
    try:
        history = version_history.get_history(APP)
        current = version_config.get_kiosk_version()
    except Exception:
        history, current, firestore_error = [], None, True
    is_published = bool(current and current["version_code"] not in ("0", "", None))
    rollout_count = 0
    if is_published:
        rollout_count = devices.count_on_version(
            all_devices, devices.APP_PACKAGES[APP], current["version_code"]
        )

    return templates.TemplateResponse(
        request,
        "versions.html",
        {
            "current": current,
            "is_published": is_published,
            "history": history,
            "rollout_count": rollout_count,
            "total_devices": len(all_devices),
            "error": error,
            "firestore_error": firestore_error,
            "published": bool(published),
            "active_page": "versions",
        },
    )


@app.post("/versions/publish", dependencies=[require_login])
def publish_version(request: Request, apk_file: UploadFile = File(...)):
    try:
        apk_url, apk_sha256, version_code, version_name = apk_storage.upload_apk(apk_file)

        # Every publish is mandatory - there's no supported "optional update" UX on the device.
        is_mandatory = True
        version_config.publish_kiosk_version(version_code, version_name, apk_url, apk_sha256, is_mandatory)
        version_history.record_publish(
            APP,
            version_code,
            version_name,
            apk_url,
            apk_sha256,
            is_mandatory,
            published_by=request.session.get("username", "?"),
        )
    except Exception as e:
        return RedirectResponse(f"/versions?error={quote(str(e))}", status_code=303)
    return RedirectResponse("/versions?published=1", status_code=303)


@app.get("/analitika", dependencies=[require_login])
def analytics_page(request: Request):
    return templates.TemplateResponse(
        request,
        "analytics.html",
        {
            "kpis": analytics.get_kpis(),
            "line": analytics.get_line_chart(),
            "bars": analytics.get_bar_chart(),
            "donut": analytics.get_donut(),
            "active_page": "analitika",
        },
    )
