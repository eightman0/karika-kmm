# Karika Kiosk Admin Dashboard

FastAPI admin tool for the Android kiosk fleet (`salesrep` + `launcher`). Talks directly to
Firebase (Firestore, Storage, Remote Config) - no separate database of its own.

Currently a subdirectory of the `karika-kmm` monorepo so it can share context (Firestore schema,
Remote Config parameter names) with the mobile code while both are moving. It has no dependency
on the rest of the repo - moving it to its own repo later is just copying this directory.

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
- **Device detail** (`/devices/{id}`) - request a fresh log pull (sets `logRequestedAt`, which the
  launcher's real-time Firestore listener picks up), download the last uploaded log via a
  server-generated signed Storage URL - the log itself isn't publicly readable.
- **Versions** (`/versions`) - view and publish the `salesrep` version Remote Config parameters
  the launcher's silent-update pipeline reads (`kiosk_version_code`, `kiosk_apk_url`, etc.).

## Why `app/remote_config.py` calls the REST API directly

The Python Admin SDK's `firebase_admin.remote_config` module only supports the newer
"Remote Config for servers" feature (`get_server_template`/`init_server_template`) - it has no
`get_template`/`publish_template` for the classic, client-facing template that `fetchAndActivate()`
reads on Android. That classic-template management API only exists in the Node.js and Java Admin
SDKs. For Python, the only option is the REST API
(`https://firebaseremoteconfig.googleapis.com/v1/projects/{project}/remoteConfig`), authenticated
with an OAuth token minted from the same service account credentials - which is what
`app/remote_config.py` does.

## Security rules

`../firestore.rules` and `../storage.rules` at the repo root are drafts written for the *mobile
apps'* access (they authenticate anonymously as a specific device). This dashboard authenticates
as the service account instead - full access, not subject to those rules at all - so it doesn't
need them, but review those files before the mobile side goes anywhere near production traffic.
