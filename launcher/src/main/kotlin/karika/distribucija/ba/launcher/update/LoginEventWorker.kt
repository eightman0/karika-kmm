package karika.distribucija.ba.launcher.update

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.provision.DeviceConfig

/** Forwards a login event salesrep broadcast to KioskEventReceiver on to the device API (Karika
 * Ops shows it as "Zadnji ulogovan"). Run as a
 * Worker (not inline in the receiver) since a plain BroadcastReceiver's onReceive() isn't meant to
 * do network I/O directly - it has no background-execution allowance the way a
 * FirebaseMessagingService does. */
class LoginEventWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val email = inputData.getString(KEY_EMAIL) ?: return Result.failure()
        val timestamp = inputData.getString(KEY_TIMESTAMP)
        return try {
            DeviceApi.reportLoginEvent(applicationContext, email, timestamp)
            Result.success()
        } catch (e: DeviceNotEnrolledException) {
            // Still enrolling: worth waiting for. Re-provisioning required: nowhere to send it.
            if (DeviceConfig.hasPendingEnrollment(applicationContext)) Result.retry() else Result.failure()
        } catch (e: DeviceApiException) {
            Log.w(TAG, "Failed to report login event: ${e.message}")
            if (e.isRetryable) Result.retry() else Result.failure()
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
