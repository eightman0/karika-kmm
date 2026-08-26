from urllib.parse import quote

from fastapi import Depends, FastAPI, File, Form, Request, UploadFile
from fastapi.responses import RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware

from . import (
    analytics,
    apk_storage,
    auth,
    device_api,
    devices,
    local_db,
    provisioning,
    version_config,
    version_history,
)

local_db.init_db()

app = FastAPI(title="Karika Kiosk Admin")
app.add_middleware(SessionMiddleware, secret_key=auth.SESSION_SECRET, same_site="lax")
app.mount("/static", StaticFiles(directory="app/static"), name="static")
app.include_router(device_api.router)
templates = Jinja2Templates(directory="app/templates")

require_login = Depends(auth.require_login)

APP = "salesrep"  # the only app published through this dashboard - launcher is Device Owner,
# changes rarely, and is deliberately not self-updated this way


@app.get("/")
def root():
    return RedirectResponse("/devices")


@app.get("/login")
def login_page(request: Request, error: str | None = None):
    device_count = len(devices.list_devices())
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
def devices_page(request: Request, q: str = "", analytics_sent: str | None = None):
    all_devices = devices.list_devices()
    filtered = devices.filter_devices(all_devices, q)
    latest_salesrep_code = version_config.get_kiosk_version()["version_code"]
    return templates.TemplateResponse(
        request,
        "devices.html",
        {
            "devices": filtered,
            "summary": devices.fleet_summary(all_devices),
            "q": q,
            "latest_salesrep_code": latest_salesrep_code,
            "active_page": "devices",
            "analytics_sent": bool(analytics_sent),
        },
    )


@app.post("/devices/analytics-request", dependencies=[require_login])
def request_analytics_all():
    devices.request_analytics_all()
    return RedirectResponse("/devices?analytics_sent=1", status_code=303)


@app.get("/devices/{device_id}", dependencies=[require_login])
def device_detail_page(
    request: Request,
    device_id: str,
    reset_error: str | None = None,
    reset_sent: str | None = None,
    cmd_error: str | None = None,
    cmd_sent: str | None = None,
):
    device = devices.get_device(device_id)
    if device is None:
        return RedirectResponse("/devices")
    return templates.TemplateResponse(
        request,
        "device_detail.html",
        {
            "device": device,
            "active_page": "devices",
            "reset_error": reset_error,
            "reset_sent": bool(reset_sent),
            "cmd_error": cmd_error,
            "cmd_sent": cmd_sent,
            "command_log": devices.command_log(device_id),
        },
    )


@app.post("/devices/{device_id}/request-logs", dependencies=[require_login])
def request_logs(device_id: str):
    devices.request_logs(device_id)
    return RedirectResponse(f"/devices/{device_id}", status_code=303)


@app.post("/devices/{device_id}/delete", dependencies=[require_login])
def delete_device(device_id: str):
    devices.delete_device(device_id)
    return RedirectResponse("/devices", status_code=303)


@app.post("/devices/{device_id}/factory-reset", dependencies=[require_login])
def factory_reset_device(device_id: str):
    try:
        devices.request_factory_reset(device_id)
    except Exception as e:
        return RedirectResponse(f"/devices/{device_id}?reset_error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/devices/{device_id}?reset_sent=1", status_code=303)


@app.post("/devices/{device_id}/reboot", dependencies=[require_login])
def reboot_device(device_id: str):
    try:
        devices.request_reboot(device_id)
    except Exception as e:
        return RedirectResponse(f"/devices/{device_id}?cmd_error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/devices/{device_id}?cmd_sent=reboot", status_code=303)


@app.post("/devices/{device_id}/exit-kiosk", dependencies=[require_login])
def exit_kiosk_device(device_id: str):
    try:
        devices.request_exit_kiosk(device_id)
    except Exception as e:
        return RedirectResponse(f"/devices/{device_id}?cmd_error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/devices/{device_id}?cmd_sent=exit_kiosk", status_code=303)


@app.post("/devices/{device_id}/maintenance", dependencies=[require_login])
def maintenance_device(device_id: str, enable: str = Form(...)):
    try:
        devices.request_maintenance(device_id, enable == "on")
    except Exception as e:
        return RedirectResponse(f"/devices/{device_id}?cmd_error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/devices/{device_id}?cmd_sent=maintenance_{enable}", status_code=303)


@app.post("/devices/{device_id}/reboot-schedule", dependencies=[require_login])
def set_reboot_schedule(device_id: str, hour: int = Form(...)):
    try:
        devices.request_reboot_schedule(device_id, hour)
    except Exception as e:
        return RedirectResponse(f"/devices/{device_id}?cmd_error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/devices/{device_id}?cmd_sent=reboot_schedule", status_code=303)


@app.post("/devices/{device_id}/mapping", dependencies=[require_login])
def set_device_mapping(device_id: str, customer_id: str = Form(""), site_id: str = Form("")):
    devices.set_device_mapping(device_id, customer_id.strip(), site_id.strip())
    return RedirectResponse(f"/devices/{device_id}", status_code=303)


@app.get("/devices/{device_id}/log", dependencies=[require_login])
def download_log(device_id: str):
    device = devices.get_device(device_id)
    path = device.get("lastLogUploadPath") if device else None
    if not path:
        return RedirectResponse(f"/devices/{device_id}")
    return RedirectResponse(devices.signed_log_url(path))


@app.get("/devices/{device_id}/analytics", dependencies=[require_login])
def download_analytics(device_id: str):
    device = devices.get_device(device_id)
    path = device.get("lastAnalyticsUploadPath") if device else None
    if not path:
        return RedirectResponse(f"/devices/{device_id}")
    return RedirectResponse(devices.signed_analytics_url(path))


@app.get("/versions", dependencies=[require_login])
def versions_page(request: Request, error: str | None = None, published: str | None = None):
    all_devices = devices.list_devices()
    history = version_history.get_history(APP)
    current = version_config.get_kiosk_version()
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


@app.get("/provisioning", dependencies=[require_login])
def provisioning_page(request: Request, generated: str | None = None):
    saved = local_db.get_provisioning_extras() or {}
    customer_id = saved.get("customer_id") or ""
    site_id = saved.get("site_id") or ""
    if not provisioning.QR_PATH.exists():
        provisioning.generate_qr(customer_id or None, site_id or None)
    return templates.TemplateResponse(
        request,
        "provisioning.html",
        {
            "provisioning_json": provisioning.build_json(customer_id or None, site_id or None),
            "customer_id": customer_id,
            "site_id": site_id,
            "qr_version": int(provisioning.QR_PATH.stat().st_mtime),
            "generated": bool(generated),
            "active_page": "provisioning",
        },
    )


@app.post("/provisioning/generate", dependencies=[require_login])
def generate_provisioning_qr(customer_id: str = Form(""), site_id: str = Form("")):
    customer_id = customer_id.strip() or None
    site_id = site_id.strip() or None
    local_db.set_provisioning_extras(customer_id, site_id)
    provisioning.generate_qr(customer_id, site_id)
    return RedirectResponse("/provisioning?generated=1", status_code=303)


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
            "top_screens": analytics.get_top_screens(),
            "top_clicks": analytics.get_top_clicks(),
            "active_page": "analitika",
        },
    )
