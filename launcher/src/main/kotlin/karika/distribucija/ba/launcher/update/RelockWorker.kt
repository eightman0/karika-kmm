package karika.distribucija.ba.launcher.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.LauncherActivity

/**
 * Fired once, RemoteDebugUnlock.DURATION_MINUTES after RemoteDebugUnlock.begin() - forces the
 * launcher back to the foreground so its onResume() re-checks RemoteDebugUnlock.isActive() (now
 * expired) and re-engages lock task, even if nothing else would have brought the launcher back to
 * front by then (salesrep sitting idle in front doesn't trigger this on its own).
 */
class RelockWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        LauncherActivity.bringToFront(applicationContext)
        return Result.success()
    }
}
