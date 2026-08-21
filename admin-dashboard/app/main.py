from urllib.parse import quote

from fastapi import FastAPI, Form, Request
from fastapi.responses import RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

from . import devices, remote_config

app = FastAPI(title="Karika Kiosk Admin")
app.mount("/static", StaticFiles(directory="app/static"), name="static")
templates = Jinja2Templates(directory="app/templates")


@app.get("/")
def root():
    return RedirectResponse("/devices")


@app.get("/devices")
def devices_page(request: Request):
    return templates.TemplateResponse(
        request, "devices.html", {"devices": devices.list_devices()}
    )


@app.get("/devices/{device_id}")
def device_detail_page(request: Request, device_id: str):
    device = devices.get_device(device_id)
    if device is None:
        return RedirectResponse("/devices")
    return templates.TemplateResponse(
        request, "device_detail.html", {"device": device}
    )


@app.post("/devices/{device_id}/request-logs")
def request_logs(device_id: str):
    devices.request_logs(device_id)
    return RedirectResponse(f"/devices/{device_id}", status_code=303)


@app.get("/devices/{device_id}/log")
def download_log(device_id: str):
    device = devices.get_device(device_id)
    path = device.get("lastLogUploadPath") if device else None
    if not path:
        return RedirectResponse(f"/devices/{device_id}")
    return RedirectResponse(devices.signed_log_url(path))


@app.get("/versions")
def versions_page(request: Request, error: str | None = None):
    current = remote_config.get_kiosk_version()
    return templates.TemplateResponse(
        request, "versions.html", {"current": current, "error": error}
    )


@app.post("/versions/publish")
def publish_version(
    version_code: str = Form(...),
    version_name: str = Form(...),
    apk_url: str = Form(...),
    apk_sha256: str = Form(""),
    mandatory: str | None = Form(None),
):
    try:
        remote_config.publish_kiosk_version(
            version_code, version_name, apk_url, apk_sha256, mandatory is not None
        )
    except Exception as e:
        return RedirectResponse(f"/versions?error={quote(str(e))}", status_code=303)
    return RedirectResponse("/versions", status_code=303)
