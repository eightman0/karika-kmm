package karika.distribucija.ba.launcher

import android.app.Application
import android.content.Intent
import android.os.Process
import karika.distribucija.ba.launcher.diagnostics.LogUploadManager
import karika.distribucija.ba.launcher.update.RemoteConfigProvider
import karika.distribucija.ba.launcher.update.UpdateScheduler
import karika.distribucija.ba.logging.AppLogger
import kotlin.system.exitProcess

class LauncherApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        RemoteConfigProvider.init(this)
        UpdateScheduler.schedulePeriodic(this)
        LogUploadManager.start(this)
        installCrashRecovery()
    }

    /**
     * The launcher is Device Owner, so its own crash recovery matters more than any payload
     * app's: relaunch LauncherActivity on an uncaught exception rather than trusting the OEM's
     * lock-task implementation alone. CrashLoopGuard caps relaunches so a startup crash can't
     * spin an unattended device forever.
     */
    private fun installCrashRecovery() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e(TAG, "Uncaught exception on ${thread.name}, recovering", throwable)
            if (CrashLoopGuard.shouldRelaunch(this)) {
                startActivity(
                    Intent(this, LauncherActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else {
                AppLogger.e(TAG, "Too many crashes in a short window, letting the process die without relaunching")
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    companion object {
        private const val TAG = "LauncherApp"
    }
}
