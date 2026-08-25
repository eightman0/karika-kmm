package karika.distribucija.ba.launcher

import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Process
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver
import karika.distribucija.ba.launcher.update.KioskMessagingService
import karika.distribucija.ba.launcher.update.UpdateScheduler
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.logging.AppLogger
import kotlin.system.exitProcess

class LauncherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        AnalyticsTracker.init(this)
        Firebase.messaging.subscribeToTopic(KioskMessagingService.BROADCAST_TOPIC)
        Firebase.messaging.subscribeToTopic(KioskMessagingService.deviceTopic(DeviceIdentity.id(this)))
        UpdateScheduler.schedulePeriodic(this)
        installCrashRecovery()
    }

    /**
     * The launcher is Device Owner, so its own crash recovery matters more than any payload
     * app's: relaunch LauncherActivity on an uncaught exception rather than trusting the OEM's
     * lock-task implementation alone. CrashLoopGuard caps quick in-process relaunches so a
     * startup crash can't spin in a tight loop - but once that cap is hit, escalate to a full
     * device reboot instead of just giving up. A kiosk that sits there with nothing running,
     * locked into lock task with no way out for whoever's in front of it, is a worse failure
     * mode than one that reboots every few minutes until it recovers.
     */
    private fun installCrashRecovery() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e(TAG, "Uncaught exception on ${thread.name}, recovering", throwable)
            if (CrashLoopGuard.shouldRelaunch(this)) {
                startActivity(
                    Intent(this, LauncherActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else if (CrashLoopGuard.shouldReboot(this)) {
                AppLogger.e(TAG, "Too many crashes in a short window, rebooting the device")
                rebootDevice()
            } else {
                AppLogger.e(TAG, "Crashing again shortly after a reboot - backing off before trying again")
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    private fun rebootDevice() {
        try {
            val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(this)
            devicePolicyManager.reboot(admin)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Reboot request failed", e)
        }
    }

    companion object {
        private const val TAG = "LauncherApp"
    }
}
