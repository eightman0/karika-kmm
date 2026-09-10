import json
from urllib.parse import quote

from fastapi import Depends, FastAPI, File, Form, Request, UploadFile
from fastapi.responses import RedirectResponse, Response
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware

from . import (
    analytics,
    apk_storage,
    auth,
    device_api,
    devices,
    launcher_version_config,
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

APP = "salesrep"  # the payload app - launcher is versioned/published the same way now too (see
# launcher_version_config.py), just through separate parallel tables/routes, not this constant


def _device_action_redirect(
    device_id: str, origin: str, q: str, toast: str, detail_qs: str = ""
) -> RedirectResponse:
    """Where a per-device action route sends the admin back to. Triggered from the devices list
    (a hidden origin=list field on that row's form, see devices.html) it stays on the list with a
    snackbar (base.html reads the `toast` query param) instead of navigating into the device's own
    page - the whole point of this being separate from the "detail" case below, which keeps its
    existing query-string banner (`detail_qs`, e.g. "cmd_sent=reboot") unchanged."""
    if origin == "list":
        params = [f"q={quote(q)}"] if q else []
        params.append(f"toast={quote(toast)}")
        return RedirectResponse(f"/devices?{'&'.join(params)}", status_code=303)
    url = f"/devices/{device_id}"
    if detail_qs:
        url += f"?{detail_qs}"
    return RedirectResponse(url, status_code=303)


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
def devices_page(request: Request, q: str = ""):
    all_devices = devices.list_devices()
    filtered = devices.filter_devices(all_devices, q)
    latest_salesrep_code = version_config.highest_known_version_code()
    return templates.TemplateResponse(
        request,
        "devices.html",
        {
            "devices": filtered,
            "summary": devices.fleet_summary(all_devices),
            "q": q,
            "latest_salesrep_code": latest_salesrep_code,
            "available_versions": version_history.get_available_versions(APP),
            "available_launcher_versions": version_history.get_available_versions("launcher"),
            "active_page": "devices",
        },
    )


@app.post("/devices/analytics-request", dependencies=[require_login])
def request_analytics_all(q: str = Form("")):
    devices.request_analytics_all()
    return _device_action_redirect("", "list", q, "Zahtjev za analitiku poslan svim uređajima.")


@app.post("/devices/update-selected", dependencies=[require_login])
def update_selected_devices(
    request: Request, device_ids: list[str] = Form(default=[]), version_code: str = Form(""),
    q: str = Form(""),
):
    if not device_ids:
        return RedirectResponse("/devices", status_code=303)
    devices.request_update_check_bulk(
        device_ids, version_code or None, request.session.get("username", "?")
    )
    return _device_action_redirect(
        "", "list", q, f"Zahtjev za update poslan na {len(device_ids)} izabrana uređaja."
    )


@app.post("/devices/update-launcher-selected", dependencies=[require_login])
def update_launcher_selected_devices(
    request: Request, device_ids: list[str] = Form(default=[]), version_code: str = Form(""),
    q: str = Form(""),
):
    if not device_ids:
        return RedirectResponse("/devices", status_code=303)
    devices.request_update_launcher_bulk(
        device_ids, version_code or None, request.session.get("username", "?")
    )
    return _device_action_redirect(
        "", "list", q, f"Zahtjev za update launcher-a poslan na {len(device_ids)} izabrana uređaja."
    )


@app.get("/devices/{device_id}", dependencies=[require_login])
def device_detail_page(
    request: Request,
    device_id: str,
    reset_error: str | None = None,
    reset_sent: str | None = None,
    cmd_error: str | None = None,
    cmd_sent: str | None = None,
    date: str | None = None,
):
    device = devices.get_device(device_id)
    if device is None:
        return RedirectResponse("/devices")
    # Unfiltered (not just versions newer than installed) - a device already on the only
    # published version still needs something explicit to pick, otherwise "Ažuriraj sada" falls
    # back to sending whatever's currently staged, which can be nothing at all.
    available_versions = version_history.get_available_versions(APP)
    selected_date, day_locations = devices.locations_for_day(device_id, date)
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
            "latest_salesrep_code": version_config.highest_known_version_code(),
            "available_versions": available_versions,
            "available_launcher_versions": version_history.get_available_versions("launcher"),
            "latest_launcher_code": launcher_version_config.highest_known_launcher_version_code(),
            "latest_location": devices.latest_location(device_id),
            "selected_date": selected_date,
            "day_locations": day_locations,
            "day_route_maps_url": devices.google_maps_route_url(day_locations),
            "day_locations_json": json.dumps(
                [
                    {
                        "lat": p["lat"],
                        "lon": p["lon"],
                        "ts": p["ts"].strftime("%H:%M:%S") if p["ts"] else "",
                    }
                    for p in day_locations
                ]
            ),
        },
    )


@app.post("/devices/{device_id}/request-logs", dependencies=[require_login])
def request_logs(device_id: str, origin: str = Form("detail"), q: str = Form("")):
    devices.request_logs(device_id)
    return _device_action_redirect(device_id, origin, q, "Zahtjev za nove logove poslan.")


@app.post("/devices/{device_id}/delete", dependencies=[require_login])
def delete_device(device_id: str, q: str = Form("")):
    devices.delete_device(device_id)
    return _device_action_redirect(device_id, "list", q, f"Uređaj {device_id} obrisan.")


@app.post("/devices/{device_id}/factory-reset", dependencies=[require_login])
def factory_reset_device(device_id: str, origin: str = Form("detail"), q: str = Form("")):
    try:
        devices.request_factory_reset(device_id)
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"reset_error={quote(str(e))}")
    return _device_action_redirect(device_id, origin, q, "Zahtjev za factory reset poslan.", "reset_sent=1")


@app.post("/devices/{device_id}/update", dependencies=[require_login])
def update_device(
    device_id: str, request: Request, version_code: str = Form(""),
    origin: str = Form("detail"), q: str = Form(""),
):
    try:
        devices.request_update_check(
            device_id, version_code or None, request.session.get("username", "?")
        )
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"cmd_error={quote(str(e))}")
    return _device_action_redirect(device_id, origin, q, "Zahtjev za update poslan.", "cmd_sent=update")


@app.post("/devices/{device_id}/reboot", dependencies=[require_login])
def reboot_device(device_id: str, origin: str = Form("detail"), q: str = Form("")):
    try:
        devices.request_reboot(device_id)
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"cmd_error={quote(str(e))}")
    return _device_action_redirect(device_id, origin, q, "Restart poslan.", "cmd_sent=reboot")


@app.post("/devices/{device_id}/open-settings", dependencies=[require_login])
def open_settings_device(device_id: str, origin: str = Form("detail"), q: str = Form("")):
    try:
        devices.request_open_settings(device_id)
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"cmd_error={quote(str(e))}")
    return _device_action_redirect(device_id, origin, q, "Komanda za Settings poslana.", "cmd_sent=open_settings")


@app.post("/devices/{device_id}/update-launcher", dependencies=[require_login])
def update_launcher_device(
    device_id: str, request: Request, version_code: str = Form(""),
    origin: str = Form("detail"), q: str = Form(""),
):
    try:
        devices.request_update_launcher(
            device_id, version_code or None, request.session.get("username", "?")
        )
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"cmd_error={quote(str(e))}")
    return _device_action_redirect(
        device_id, origin, q, "Zahtjev za update launcher-a poslan.", "cmd_sent=update_launcher"
    )


@app.post("/devices/{device_id}/ping", dependencies=[require_login])
def ping_device(device_id: str, origin: str = Form("detail"), q: str = Form("")):
    try:
        devices.request_ping(device_id)
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"cmd_error={quote(str(e))}")
    return _device_action_redirect(device_id, origin, q, "Provjera statusa poslana.", "cmd_sent=ping")


@app.post("/devices/{device_id}/maintenance", dependencies=[require_login])
def maintenance_device(
    device_id: str, enable: str = Form(...), origin: str = Form("detail"), q: str = Form(""),
):
    try:
        devices.request_maintenance(device_id, enable == "on")
    except Exception as e:
        return _device_action_redirect(device_id, origin, q, f"Greška: {e}", f"cmd_error={quote(str(e))}")
    toast = "Maintenance uključen." if enable == "on" else "Maintenance isključen."
    return _device_action_redirect(device_id, origin, q, toast, f"cmd_sent=maintenance_{enable}")


@app.post("/devices/{device_id}/debug-unlock", dependencies=[require_login])
def debug_unlock_device(device_id: str, enable: str = Form(...)):
    try:
        devices.request_debug_unlock(device_id, enable == "on")
    except Exception as e:
        return RedirectResponse(f"/devices/{device_id}?cmd_error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/devices/{device_id}?cmd_sent=debug_unlock_{enable}", status_code=303)


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
def versions_page(
    request: Request,
    app: str = "salesrep",
    error: str | None = None,
    published: str | None = None,
    sent_all: str | None = None,
    rolled_back: str | None = None,
):
    app = app if app in ("salesrep", "launcher") else "salesrep"
    all_devices = devices.list_devices()
    history = version_history.get_history(app, limit=50)
    if app == "launcher":
        current = launcher_version_config.get_launcher_version()
        staged = launcher_version_config.get_staged_launcher_version()
        is_staged = launcher_version_config.is_launcher_staged()
        staged_target_count = launcher_version_config.staged_launcher_target_count() if is_staged else 0
    else:
        current = version_config.get_kiosk_version()
        staged = version_config.get_staged_version()
        is_staged = version_config.is_staged()
        staged_target_count = version_config.staged_target_count() if is_staged else 0
    is_published = bool(current and current["version_code"] not in ("0", "", None))
    rollout_count = 0
    if is_published:
        rollout_count = devices.count_on_version(
            all_devices, devices.APP_PACKAGES[app], current["version_code"]
        )

    return templates.TemplateResponse(
        request,
        "versions.html",
        {
            "active_app": app,
            "current": current,
            "is_published": is_published,
            "history": history,
            "rollout_count": rollout_count,
            "total_devices": len(all_devices),
            "staged": staged,
            "is_staged": is_staged,
            "staged_target_count": staged_target_count,
            "error": error,
            "published": bool(published),
            "sent_all": bool(sent_all),
            "rolled_back": bool(rolled_back),
            "active_page": "versions",
        },
    )


@app.post("/versions/publish", dependencies=[require_login])
def publish_version(request: Request, apk_file: UploadFile = File(...), app: str = Form("salesrep")):
    app = app if app in ("salesrep", "launcher") else "salesrep"
    try:
        apk_url, apk_sha256, version_code, version_name = apk_storage.upload_apk(apk_file, app)

        # Every publish is mandatory - there's no supported "optional update" UX on the device.
        is_mandatory = True
        username = request.session.get("username", "?")
        if app == "launcher":
            launcher_version_config.publish_staged_launcher_version(
                version_code, version_name, apk_url, apk_sha256, is_mandatory, username
            )
        else:
            version_config.publish_staged_version(
                version_code, version_name, apk_url, apk_sha256, is_mandatory, username
            )
        version_history.record_publish(
            app,
            version_code,
            version_name,
            apk_url,
            apk_sha256,
            is_mandatory,
            published_by=username,
        )
    except Exception as e:
        return RedirectResponse(f"/versions?app={app}&error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/versions?app={app}&published=1", status_code=303)


@app.post("/versions/send-all", dependencies=[require_login])
def send_version_to_all(request: Request, app: str = Form("salesrep")):
    app = app if app in ("salesrep", "launcher") else "salesrep"
    username = request.session.get("username", "?")
    if app == "launcher":
        devices.request_update_launcher_all(username)
    else:
        devices.request_update_all(username)
    return RedirectResponse(f"/versions?app={app}&sent_all=1", status_code=303)


@app.post("/versions/rollback/{entry_id}", dependencies=[require_login])
def rollback_version(entry_id: int, request: Request):
    entry = local_db.get_history_entry_by_id(entry_id)
    app = entry["app"] if entry and entry.get("app") == "launcher" else "salesrep"
    try:
        username = request.session.get("username", "?")
        if app == "launcher":
            launcher_version_config.rollback_launcher_to_history_entry(entry_id, username)
        else:
            version_config.rollback_to_history_entry(entry_id, username)
    except Exception as e:
        return RedirectResponse(f"/versions?app={app}&error={quote(str(e))}", status_code=303)
    return RedirectResponse(f"/versions?app={app}&rolled_back=1", status_code=303)


@app.post("/versions/history/{entry_id}/delete", dependencies=[require_login])
def delete_history_entry(entry_id: int):
    entry = local_db.get_history_entry_by_id(entry_id)
    app = entry["app"] if entry and entry.get("app") == "launcher" else "salesrep"
    version_history.delete_entry(entry_id)
    return RedirectResponse(f"/versions?app={app}", status_code=303)


@app.post("/versions/history/delete-selected", dependencies=[require_login])
def delete_selected_history_entries(app: str = Form("salesrep"), entry_ids: list[int] = Form(default=[])):
    for entry_id in entry_ids:
        version_history.delete_entry(entry_id)
    return RedirectResponse(f"/versions?app={app}", status_code=303)


@app.get("/provisioning", dependencies=[require_login])
def provisioning_page(request: Request, generated: str | None = None):
    saved = local_db.get_provisioning_extras() or {}
    return templates.TemplateResponse(
        request,
        "provisioning.html",
        {
            "provisioning_json": provisioning.build_json(saved),
            "customer_id": saved.get("customer_id") or "",
            "site_id": saved.get("site_id") or "",
            "wifi_ssid": saved.get("wifi_ssid") or "",
            "wifi_password": saved.get("wifi_password") or "",
            "wifi_security_type": saved.get("wifi_security_type") or "WPA",
            "apk_download_url": saved.get("apk_download_url") or "",
            "resolved_apk_download_url": provisioning.resolve_apk_download_url(saved),
            "generated": bool(generated),
            "active_page": "provisioning",
        },
    )


@app.get("/provisioning/qr.png", dependencies=[require_login])
def provisioning_qr():
    saved = local_db.get_provisioning_extras() or {}
    png = provisioning.qr_png_bytes(saved)
    return Response(content=png, media_type="image/png", headers={"Cache-Control": "no-store"})


@app.post("/provisioning/generate", dependencies=[require_login])
def generate_provisioning_qr(
    customer_id: str = Form(""),
    site_id: str = Form(""),
    wifi_ssid: str = Form(""),
    wifi_password: str = Form(""),
    wifi_security_type: str = Form("WPA"),
    apk_download_url: str = Form(""),
):
    ssid = wifi_ssid.strip() or None
    url = apk_download_url.strip()
    local_db.set_provisioning_extras(
        customer_id.strip() or None,
        site_id.strip() or None,
        ssid,
        wifi_password if ssid else None,
        wifi_security_type if ssid else None,
        url or None,
    )
    return RedirectResponse("/provisioning?generated=1", status_code=303)


@app.get("/analitika", dependencies=[require_login])
def analytics_page(request: Request, customer_id: str = ""):
    device_ids = local_db.device_ids_for_customer(customer_id) if customer_id else None
    return templates.TemplateResponse(
        request,
        "analytics.html",
        {
            "kpis": analytics.get_kpis(device_ids),
            "line": analytics.get_line_chart(device_ids),
            "bars": analytics.get_bar_chart(device_ids),
            "donut": analytics.get_donut(device_ids),
            "top_screens": analytics.get_top_screens(device_ids),
            "top_clicks": analytics.get_top_clicks(device_ids),
            "customers": local_db.list_customer_ids(),
            "selected_customer": customer_id,
            "active_page": "analitika",
        },
    )
