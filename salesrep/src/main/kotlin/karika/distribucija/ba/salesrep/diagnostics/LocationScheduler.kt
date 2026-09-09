package karika.distribucija.ba.salesrep.diagnostics

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object LocationScheduler {
    private const val PERIODIC_WORK_NAME = "location_sample_periodic"

    // WorkManager's own floor for periodic work - can't schedule anything more frequent than this.
    private const val INTERVAL_MINUTES = 15L

    /** Call once from Application.onCreate() - safe to call on every process start. REPLACE so an
     * interval change here takes effect on the next start rather than being stuck on whatever was
     * enqueued the first time the app ever ran. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<LocationSampleWorker>(INTERVAL_MINUTES, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }
}
