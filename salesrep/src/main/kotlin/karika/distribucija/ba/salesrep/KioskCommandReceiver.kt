package karika.distribucija.ba.salesrep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import karika.distribucija.ba.logging.KioskIpc

/**
 * Receives commands from the launcher (Device Owner) - exported since it's a cross-app,
 * cross-signing-key broadcast (see KioskIpc), but only acts if EXTRA_TOKEN matches this build's
 * own compiled-in token.
 */
class KioskCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getStringExtra(KioskIpc.EXTRA_TOKEN) != BuildConfig.KIOSK_IPC_TOKEN) {
            Log.w(TAG, "Ignoring ${intent.action} - token mismatch")
            return
        }
        when (intent.action) {
            KioskIpc.ACTION_EXIT_KIOSK -> MainActivity.requestExitLockTask()
        }
    }

    private companion object {
        const val TAG = "KioskCommandReceiver"
    }
}
