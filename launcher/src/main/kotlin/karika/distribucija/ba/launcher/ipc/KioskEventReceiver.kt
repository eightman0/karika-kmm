package karika.distribucija.ba.launcher.ipc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import karika.distribucija.ba.launcher.BuildConfig
import karika.distribucija.ba.launcher.update.LoginEventWorker
import karika.distribucija.ba.logging.KioskIpc

/**
 * Receives events salesrep reports about itself - exported since it's a cross-app,
 * cross-signing-key broadcast (see KioskIpc), but only acts if EXTRA_TOKEN matches this build's
 * own compiled-in token.
 */
class KioskEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getStringExtra(KioskIpc.EXTRA_TOKEN) != BuildConfig.KIOSK_IPC_TOKEN) {
            Log.w(TAG, "Ignoring ${intent.action} - token mismatch")
            return
        }
        when (intent.action) {
            KioskIpc.ACTION_LOGIN_EVENT -> handleLoginEvent(context, intent)
        }
    }

    private fun handleLoginEvent(context: Context, intent: Intent) {
        val email = intent.getStringExtra(KioskIpc.EXTRA_USER_EMAIL) ?: return
        val timestamp = intent.getStringExtra(KioskIpc.EXTRA_LOGIN_TIMESTAMP)
        val request = OneTimeWorkRequestBuilder<LoginEventWorker>()
            .setInputData(
                workDataOf(LoginEventWorker.KEY_EMAIL to email, LoginEventWorker.KEY_TIMESTAMP to timestamp)
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private companion object {
        const val TAG = "KioskEventReceiver"
    }
}
