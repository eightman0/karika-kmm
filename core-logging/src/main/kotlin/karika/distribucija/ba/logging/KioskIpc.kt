package karika.distribucija.ba.logging

/**
 * Broadcast contract between launcher (Device Owner) and salesrep (the kiosk UI). Not gated by a
 * signature-level permission - the two apps are signed with different release keys, so that
 * protection level can never resolve between them. Each side instead checks EXTRA_TOKEN against
 * its own BuildConfig.KIOSK_IPC_TOKEN (identical literal baked into both builds) before acting -
 * the same "verify in code, don't trust the platform's grant" approach LogProvider uses for the
 * log/analytics pull, after a manifest permission proved unreliable there.
 */
object KioskIpc {
    const val LAUNCHER_PACKAGE = "karika.distribucija.ba.launcher"

    /** Salesrep -> launcher: a user just logged in. */
    const val ACTION_LOGIN_EVENT = "karika.distribucija.ba.kiosk.action.LOGIN_EVENT"

    const val EXTRA_TOKEN = "token"
    const val EXTRA_USER_EMAIL = "userEmail"
    const val EXTRA_USER_ID = "userId"
    const val EXTRA_LOGIN_TIMESTAMP = "loginTimestamp"
}
