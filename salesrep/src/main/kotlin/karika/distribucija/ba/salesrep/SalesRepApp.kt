package karika.distribucija.ba.salesrep

import android.app.Application
import android.content.Intent
import android.os.Process
import android.util.Log
import karika.distribucija.ba.salesrep.session.SessionManager
import karika.distribucija.ba.salesrep.update.RemoteConfigProvider
import karika.distribucija.ba.salesrep.update.UpdateScheduler
import kotlin.system.exitProcess

class SalesRepApp : Application() {
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        sessionManager.restoreTokenIfPresent()
        RemoteConfigProvider.init(this)
        UpdateScheduler.schedulePeriodic(this)
        installCrashRecovery()
    }

    /**
     * Backs up Android's own lock-task crash recovery: on an uncaught exception, relaunch
     * MainActivity ourselves before the process dies, rather than trusting every OEM's lock-task
     * implementation to restart the pinned task promptly. CrashLoopGuard stops this from turning
     * a startup crash into an infinite relaunch loop on an unattended device.
     */
    private fun installCrashRecovery() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception on ${thread.name}, recovering", throwable)
            if (CrashLoopGuard.shouldRelaunch(this)) {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            } else {
                Log.e(TAG, "Too many crashes in a short window, letting the process die without relaunching")
            }
            Process.killProcess(Process.myPid())
            exitProcess(10)
        }
    }

    companion object {
        private const val TAG = "SalesRepApp"
    }
}
