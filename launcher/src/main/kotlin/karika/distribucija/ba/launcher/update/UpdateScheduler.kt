package karika.distribucija.ba.launcher.update

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

object UpdateScheduler {
    private const val PERIODIC_WORK_NAME = "payload_update_check_periodic"
    private const val IMMEDIATE_WORK_NAME = "payload_update_check_immediate"
    private const val PERIODIC_INTERVAL_MINUTES = 30L

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Call once from Application.onCreate() - safe to call on every process start. REPLACE (not
     * KEEP) so every process start - cold boot, crash-recovery relaunch, reboot - runs a check
     * right away instead of waiting out whatever's left of a previously-scheduled interval; it
     * also means an interval change here takes effect on the next start rather than being stuck
     * on whatever was enqueued the first time the app ever ran. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<UpdateWorker>(PERIODIC_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setConstraints(networkConstraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    /** Fired when Remote Config's real-time listener sees a published change, so devices don't
     * have to wait for the next periodic tick. */
    fun triggerImmediateCheck(context: Context) {
        val request = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
