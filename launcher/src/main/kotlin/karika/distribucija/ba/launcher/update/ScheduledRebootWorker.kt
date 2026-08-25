package karika.distribucija.ba.launcher.update

import android.app.admin.DevicePolicyManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Reboots the device once a day at a configurable hour - not for recovery (ExitKioskFlow/the
 * crash-loop escalation in LauncherApp already handle that), just routine hygiene for a device
 * that's expected to run unattended for months. WorkManager's periodic interval doesn't let you
 * pin a wall-clock time directly, so schedule() computes an initialDelay that lands the first run
 * on the target hour - every run after that is exactly 24h later, which keeps it aligned (modulo
 * whatever slop WorkManager/Doze already tolerate for periodic work in general).
 */
class ScheduledRebootWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val devicePolicyManager = applicationContext.getSystemService(DevicePolicyManager::class.java)
        val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(applicationContext)
        runCatching { devicePolicyManager.reboot(admin) }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "scheduled_reboot"
        private const val PREFS = "scheduled_reboot"
        private const val KEY_HOUR = "hour"
        private const val DEFAULT_HOUR = 4

        /** Call once from Application.onCreate() - re-reads whatever hour was last configured
         * (or DEFAULT_HOUR if never set) and (re)schedules from there. */
        fun scheduleFromSavedHour(context: Context) {
            schedule(context, savedHour(context))
        }

        /** Called from the set_reboot_schedule FCM command. */
        fun reschedule(context: Context, hourOfDay: Int) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt(KEY_HOUR, hourOfDay)
                .apply()
            schedule(context, hourOfDay)
        }

        fun savedHour(context: Context): Int =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_HOUR, DEFAULT_HOUR)

        private fun schedule(context: Context, hourOfDay: Int) {
            val initialDelayMs = millisUntilNextOccurrence(hourOfDay)
            val request = PeriodicWorkRequestBuilder<ScheduledRebootWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        private fun millisUntilNextOccurrence(hourOfDay: Int): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}
