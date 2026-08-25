# Karika Kiosk Admin Dashboard

FastAPI admin tool for the Android kiosk fleet (`salesrep` + `launcher`). Fleet state and the
published version live in a local SQLite file (`app/local_db.py`) - not Firestore. Firestore's
client is gRPC-only on both this app and the launcher, with no REST fallback on either, and a bad
network moment blocking gRPC while plain HTTPS kept working is exactly what pushed this off
Firestore. Firebase Storage (APK hosting, log files) and Cloud Messaging (push) are still used -
both are plain-HTTPS already, never actually part of that problem.

Currently a subdirectory of the `karika-kmm` monorepo so it can share context with the mobile code
while both are moving. It has no dependency on the rest of the repo - moving it to its own repo
later is just copying this directory (plus whatever's in `/etc/karika-admin-dashboard/data` on the
VPS, see below).

## Setup

1. `python -m venv .venv && source .venv/bin/activate`
2. `pip install -r requirements.txt`
3. Firebase Console -> Project Settings -> Service Accounts -> Generate new private key, save it
   as `serviceAccountKey.json` in this directory (gitignored - never commit it).
4. `cp .env.example .env` and fill in your project ID / storage bucket.
5. `uvicorn app.main:app --reload`

## Features

- **Devices** (`/devices`) - every device that has self-registered (via a heartbeat or a log
  upload from the launcher app): installed `salesrep` version, last-seen status.
- **Device detail** (`/devices/{id}`) - request a fresh log pull (records the request and sends an
  FCM push targeted at that one device - see `app/push.py` - which the launcher picks up even from
  a frozen/stopped process), download the last uploaded log via a server-generated signed Storage
  URL - the log itself isn't publicly readable.
- **Versions** (`/versions`) - view and publish the `salesrep` version the launcher's silent-update
  pipeline reads over its own `/api/version` endpoint (`app/device_api.py`). Publishing also sends
  an FCM data message so devices check right away instead of waiting for their next periodic poll
  (at most ~30 min later).
- **Login** (`/login`) - session-cookie auth gates every other page. Defaults to `admin`/`admin`
  (see `ADMIN_USERNAME`/`ADMIN_PASSWORD` in `.env.example`) - change those before this is
  anything but a throwaway internal tool. Set `SESSION_SECRET` too, or every deploy (which
  restarts the container) logs everyone out.

## Deploy (Contabo VPS, Docker)

`.github/workflows/deploy-admin-dashboard.yml` pushes `admin-dashboard/` to the VPS via
`appleboy/scp-action` on every push to `develop` that touches this directory, then SSHes in
(`appleboy/ssh-action`) and runs `docker compose up -d --build`. Requires Docker + the Compose
plugin already on the VPS (same as whatever else you're running there).

`docker-compose.yml` binds the container to `127.0.0.1:8000` only - **not** a public port.
`deploy/nginx.conf.example` is the actual config used for `karika.car4hire.ba` (reverse proxy +
`certbot --nginx` for TLS) - copy it to `/etc/nginx/sites-available/` by hand, it's not deployed
by the workflow. The app's own login (above) is what actually gates access now - nginx has no
`auth_basic` in front of it, which is fine as a second layer but not a substitute for changing
`ADMIN_PASSWORD` off the default.

**One-time VPS setup (not automated - do this once by hand):**

1. `sudo mkdir -p /opt/karika-admin-dashboard /etc/karika-admin-dashboard/data`
2. Put your Firebase service account key at
   `/etc/karika-admin-dashboard/serviceAccountKey.json` - `docker-compose.yml` bind-mounts it
   into the container read-only, it's never baked into the image.
3. `/etc/karika-admin-dashboard/data` is where `docker-compose.yml` bind-mounts the SQLite file
   (`local_db.py`'s `LOCAL_DB_PATH`, default `data/karika.db` relative to the container's
   `/app`) - it has to survive `docker compose up -d --build` rebuilding the container on every
   deploy, so it lives here rather than inside the image.
4. Create `/etc/karika-admin-dashboard/env` (`KEY=VALUE` per line, no quotes/`export` - this is
   Compose's `env_file`, and the path here is the container-internal path, not the host one):
   ```
   FIREBASE_SERVICE_ACCOUNT_PATH=/run/secrets/serviceAccountKey.json
   FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=change-me
   SESSION_SECRET=paste-a-generated-secrets.token_hex(32)-here
   ```
5. Set the repo's GitHub Actions secrets: `VPS_HOST`, `VPS_USER`, `SSH_PRIVATE_KEY`, and
   optionally `SSH_PORT` (defaults to 22).

The workflow wipes and re-copies `/opt/karika-admin-dashboard` on every deploy - secrets live
outside it in `/etc/karika-admin-dashboard/` specifically so a deploy can never delete them.
Docker's build cache lives in the daemon, not that directory, so rebuilds still skip the `pip
install` layer when `requirements.txt` hasn't changed. Adjust the trigger branch in the workflow
if `develop` isn't this repo's deploy branch.

## Security rules

`../storage.rules` at the repo root is a draft written for the *mobile app's* access (it
authenticates anonymously as a specific device). This dashboard authenticates as the service
account instead - full access, not subject to those rules at all - so it doesn't need it, but
review it before the mobile side goes anywhere near production traffic.
