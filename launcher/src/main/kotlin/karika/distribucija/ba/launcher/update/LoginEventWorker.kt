package karika.distribucija.ba.launcher.update

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity

/** Forwards a login event salesrep broadcast to KioskEventReceiver on to the dashboard. Run as a
 * Worker (not inline in the receiver) since a plain BroadcastReceiver's onReceive() isn't meant to
 * do network I/O directly - it has no background-execution allowance the way a
 * FirebaseMessagingService does. */
class LoginEventWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val email = inputData.getString(KEY_EMAIL) ?: return Result.failure()
        val timestamp = inputData.getString(KEY_TIMESTAMP)
        return try {
            DashboardApi.reportLoginEvent(DeviceIdentity.id(applicationContext), email, timestamp)
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to report login event", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "LoginEventWorker"
        const val KEY_EMAIL = "email"
        const val KEY_TIMESTAMP = "timestamp"
    }
}
