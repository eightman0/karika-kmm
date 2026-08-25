package karika.distribucija.ba.salesrep

import android.app.Application
import android.content.Intent
import android.os.Process
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.logging.AppLogger
import karika.distribucija.ba.salesrep.session.SessionManager
import kotlin.system.exitProcess

class SalesRepApp : Application() {
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        AnalyticsTracker.init(this)
        sessionManager = SessionManager(this)
        sessionManager.restoreTokenIfPresent()
        installCrashRecovery()
    }

    /**
     * launcher owns Device Owner / lock task / the update pipeline now, but salesrep can still
     * relaunch its own activity on crash without any special privilege - this is a plain
     * self-relaunch. CrashLoopGuard stops it from turning a startup crash into an infinite
     * relaunch loop on an unattended device.
     */
    private fun installCrashRecovery() {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            AppLogger.e(TAG, "Uncaught exception on ${thread.name}, recovering", throwable)
            if (CrashLoopGuard.shouldRelaunch(this)) {
                startActivity(
                    Intent(this, MainActivity::class.java)
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
        private const val TAG = "SalesRepApp"
    }
}
