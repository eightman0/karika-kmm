package karika.distribucija.ba.launcher.update

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
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

    private const val LAUNCHER_SELF_UPDATE_WORK_NAME = "launcher_self_update"
    private const val RELOCK_WORK_NAME = "debug_unlock_auto_relock"

    private const val HEARTBEAT_ALARM_INTERVAL_MINUTES = 30L
    private const val HEARTBEAT_ALARM_REQUEST_CODE = 1001

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

    /** Fired on CMD_UPDATE_LAUNCHER - see LauncherSelfUpdateWorker. */
    fun triggerLauncherSelfUpdate(context: Context) {
        val request = OneTimeWorkRequestBuilder<LauncherSelfUpdateWorker>()
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            LAUNCHER_SELF_UPDATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Fired on CMD_DEBUG_UNLOCK - see RemoteDebugUnlock/RelockWorker. No network constraint,
     * this never touches the network, just an activity restart. */
    fun scheduleAutoRelock(context: Context, delayMinutes: Long) {
        val request = OneTimeWorkRequestBuilder<RelockWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            RELOCK_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /** Fired on CMD_DEBUG_LOCK, so a manual re-lock before the timer elapses does not leave a
     * stale relock still pending (harmless if it fires anyway, but no reason to leave it around). */
    fun cancelAutoRelock(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(RELOCK_WORK_NAME)
    }

    /** AlarmManager backstop for the heartbeat - see HeartbeatAlarmReceiver's own doc comment for
     * why this exists as a separate path from schedulePeriodic()'s WorkManager-based one. Call
     * once from Application.onCreate(); the receiver re-arms the next fire itself, so this never
     * needs to run on a fixed repeating schedule the way a plain PeriodicWorkRequest would. */
    fun scheduleHeartbeatAlarm(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            HEARTBEAT_ALARM_REQUEST_CODE,
            Intent(context, HeartbeatAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(HEARTBEAT_ALARM_INTERVAL_MINUTES)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent)
    }
}
