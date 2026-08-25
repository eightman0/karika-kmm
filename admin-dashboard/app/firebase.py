import os
from functools import lru_cache

import firebase_admin
from dotenv import load_dotenv
from firebase_admin import credentials, storage

load_dotenv()


@lru_cache
def _app() -> firebase_admin.App:
    if firebase_admin._apps:
        return firebase_admin.get_app()
    cred = credentials.Certificate(os.environ["FIREBASE_SERVICE_ACCOUNT_PATH"])
    return firebase_admin.initialize_app(
        cred, {"storageBucket": os.environ["FIREBASE_STORAGE_BUCKET"]}
    )


def bucket():
    # Still Firebase Storage, not Firestore - plain-HTTPS resumable uploads under the hood, so it
    # was never part of the gRPC connectivity problem local_db.py's docstring explains.
    _app()
    return storage.bucket()


def init_messaging() -> None:
    """firebase_admin.messaging needs the app initialized once before first use."""
    _app()
