package karika.distribucija.ba.launcher.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.LauncherActivity
import java.util.concurrent.TimeUnit

/**
 * Forces the launcher back to the foreground once an exit-kiosk support window (KioskExitState)
 * expires, so a tablet left unattended right after support finishes doesn't stay unlocked
 * indefinitely waiting for some unrelated resume event. LauncherActivity.onResume() re-checks
 * KioskExitState itself and re-arms lock task once it sees the window has passed - this worker's
 * only job is to guarantee that resume actually happens.
 */
class ReArmKioskWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        LauncherActivity.bringToFront(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "rearm_kiosk"

        fun scheduleAt(context: Context, delayMs: Long) {
            val request = OneTimeWorkRequestBuilder<ReArmKioskWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        /** Called from LauncherApp.onCreate() - KioskExitState is already cleared by then on a
         * fresh process start, so the scheduled bring-to-front would just be redundant. */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
