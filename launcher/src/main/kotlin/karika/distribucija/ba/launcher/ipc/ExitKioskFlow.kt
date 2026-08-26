package karika.distribucija.ba.launcher.ipc

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.util.Log
import karika.distribucija.ba.launcher.BuildConfig
import karika.distribucija.ba.launcher.KioskExitState
import karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver
import karika.distribucija.ba.launcher.update.ReArmKioskWorker
import karika.distribucija.ba.logging.KioskIpc
import kotlinx.coroutines.delay

/**
 * Remote "get me out of kiosk mode" chain, triggered by the exit_kiosk FCM command - lets support
 * poke around a stuck or misbehaving device without a truck roll.
 *
 * 1. Soft: ask salesrep to call stopLockTask() on itself via a broadcast (see
 *    KioskCommandReceiver on that side).
 * 2. Hard: narrow the lock task allowlist down to just the launcher. Android exits lock task on
 *    its own once the foreground app is no longer in that list - no forceStop/hide trick needed.
 *    The full allowlist comes back automatically the next time LauncherActivity resumes, since
 *    LauncherKiosk.enter() recomputes it from scratch every time.
 * 3. Last resort: reboot.
 *
 * Without KioskExitState, none of the above would stick: LauncherActivity.onResume() re-arms
 * lock task and relaunches salesrep unconditionally, and salesrep unlocking itself is exactly
 * what brings the launcher back to the front - so the very next resume undid the exit before
 * anyone could use the open device. KioskExitState suppresses both while its window is open.
 *
 * Not yet verified end-to-end on a real device - in particular, whether
 * ActivityManager.lockTaskModeState queried from the launcher's own (backgrounded) process
 * reliably reflects the globally locked app's state rather than just the caller's own task.
 */
object ExitKioskFlow {
    private const val TAG = "ExitKioskFlow"
    private const val SOFT_TIMEOUT_MS = 10_000L
    private const val POLL_INTERVAL_MS = 500L
    private const val EXIT_WINDOW_MS = 15 * 60 * 1000L
    private const val SALESREP_RECEIVER = "karika.distribucija.ba.salesrep.KioskCommandReceiver"

    suspend fun run(context: Context) {
        val appContext = context.applicationContext
        KioskExitState.begin(appContext, EXIT_WINDOW_MS)
        ReArmKioskWorker.scheduleAt(appContext, EXIT_WINDOW_MS)
        sendSoftExitBroadcast(appContext)

        var waited = 0L
        while (waited < SOFT_TIMEOUT_MS) {
            if (!isLockTaskActive(appContext)) {
                Log.i(TAG, "Soft exit succeeded")
                return
            }
            delay(POLL_INTERVAL_MS)
            waited += POLL_INTERVAL_MS
        }

        Log.w(TAG, "Soft exit timed out, narrowing lock task allowlist to force it")
        narrowLockTaskAllowlist(appContext)

        delay(SOFT_TIMEOUT_MS)
        if (isLockTaskActive(appContext)) {
            Log.e(TAG, "Still locked after narrowing allowlist, rebooting as last resort")
            reboot(appContext)
        }
    }

    private fun sendSoftExitBroadcast(context: Context) {
        val intent = Intent(KioskIpc.ACTION_EXIT_KIOSK)
            .setClassName(KioskIpc.SALESREP_PACKAGE, SALESREP_RECEIVER)
            .putExtra(KioskIpc.EXTRA_TOKEN, BuildConfig.KIOSK_IPC_TOKEN)
        runCatching { context.sendBroadcast(intent) }
    }

    private fun isLockTaskActive(context: Context): Boolean {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        return activityManager.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
    }

    private fun narrowLockTaskAllowlist(context: Context) {
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(context)
        runCatching { devicePolicyManager.setLockTaskPackages(admin, arrayOf(context.packageName)) }
    }

    private fun reboot(context: Context) {
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(context)
        runCatching { devicePolicyManager.reboot(admin) }
    }
}
