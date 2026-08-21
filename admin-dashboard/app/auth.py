import os
import secrets

from dotenv import load_dotenv
from fastapi import HTTPException, Request
from starlette.status import HTTP_303_SEE_OTHER

load_dotenv()

# Dummy defaults so this works out of the box - override via env before this is anything but a
# throwaway internal tool. See .env.example / the README.
ADMIN_USERNAME = os.environ.get("ADMIN_USERNAME", "admin")
ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "admin")

# Falling back to a freshly generated secret means every process restart (e.g. every deploy)
# invalidates existing sessions - annoying but safe. Set SESSION_SECRET to keep people logged in
# across restarts.
SESSION_SECRET = os.environ.get("SESSION_SECRET") or secrets.token_hex(32)


def check_credentials(username: str, password: str) -> bool:
    return secrets.compare_digest(username, ADMIN_USERNAME) and secrets.compare_digest(
        password, ADMIN_PASSWORD
    )


def require_login(request: Request) -> None:
    """Route dependency - add to every page that isn't /login itself or a static asset."""
    if not request.session.get("logged_in"):
        raise HTTPException(status_code=HTTP_303_SEE_OTHER, headers={"Location": "/login"})
