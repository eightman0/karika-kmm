import os
from functools import lru_cache

import firebase_admin
from dotenv import load_dotenv
from firebase_admin import credentials, firestore, storage

load_dotenv()


@lru_cache
def _app() -> firebase_admin.App:
    if firebase_admin._apps:
        return firebase_admin.get_app()
    cred = credentials.Certificate(os.environ["FIREBASE_SERVICE_ACCOUNT_PATH"])
    return firebase_admin.initialize_app(
        cred, {"storageBucket": os.environ["FIREBASE_STORAGE_BUCKET"]}
    )


def db():
    _app()
    return firestore.client()


def bucket():
    _app()
    return storage.bucket()
