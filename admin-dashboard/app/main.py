from urllib.parse import quote

from fastapi import Depends, FastAPI, File, Form, Request, UploadFile
from fastapi.responses import RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware

from . import apk_storage, auth, devices, remote_config

app = FastAPI(title="Karika Kiosk Admin")
app.add_middleware(SessionMiddleware, secret_key=auth.SESSION_SECRET, same_site="lax")
app.mount("/static", StaticFiles(directory="app/static"), name="static")
templates = Jinja2Templates(directory="app/templates")

require_login = Depends(auth.require_login)


@app.get("/")
def root():
    return RedirectResponse("/devices")


@app.get("/login")
def login_page(request: Request, error: str | None = None):
    return templates.TemplateResponse(request, "login.html", {"error": error})


@app.post("/login")
def login_submit(request: Request, username: str = Form(...), password: str = Form(...)):
    if not auth.check_credentials(username, password):
        return RedirectResponse(
            f"/login?error={quote('Pogrešno korisničko ime ili lozinka')}", status_code=303
        )
    request.session["logged_in"] = True
    return RedirectResponse("/devices", status_code=303)


@app.get("/logout")
def logout(request: Request):
    request.session.clear()
    return RedirectResponse("/login", status_code=303)


@app.get("/devices", dependencies=[require_login])
def devices_page(request: Request):
    return templates.TemplateResponse(
        request, "devices.html", {"devices": devices.list_devices()}
    )


@app.get("/devices/{device_id}", dependencies=[require_login])
def device_detail_page(request: Request, device_id: str):
    device = devices.get_device(device_id)
    if device is None:
        return RedirectResponse("/devices")
    return templates.TemplateResponse(
        request, "device_detail.html", {"device": device}
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
def versions_page(request: Request, error: str | None = None):
    current = remote_config.get_kiosk_version()
    return templates.TemplateResponse(
        request, "versions.html", {"current": current, "error": error}
    )


@app.post("/versions/publish", dependencies=[require_login])
def publish_version(
    version_code: str = Form(...),
    version_name: str = Form(...),
    apk_url: str = Form(""),
    apk_sha256: str = Form(""),
    mandatory: str | None = Form(None),
    apk_file: UploadFile | None = File(None),
):
    try:
        if apk_file is not None and apk_file.filename:
            apk_url, apk_sha256 = apk_storage.upload_apk(apk_file, version_code)
        elif not apk_url:
            raise ValueError("Uploaduj APK fajl ili unesi direktan URL do njega")

        remote_config.publish_kiosk_version(
            version_code, version_name, apk_url, apk_sha256, mandatory is not None
        )
    except Exception as e:
        return RedirectResponse(f"/versions?error={quote(str(e))}", status_code=303)
    return RedirectResponse("/versions", status_code=303)
