package karika.distribucija.ba

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import karika.distribucija.ba.provision.KarikaKiosk
import karika.distribucija.ba.ui.common.isKiosk


class MainActivityKiosk : KarikaActivity(), InstallStateUpdatedListener {
    private lateinit var kiosk: KarikaKiosk
    private val idleTimeout = 20000L
    private val handler = Handler(Looper.getMainLooper())
    private val idleRunnable = Runnable {
        showScreensaver()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kiosk = KarikaKiosk(this)
        appUpdateManager.registerListener(this)
    }

    override fun onStateUpdate(p0: InstallState) {
        if (p0.installStatus() == InstallStatus.DOWNLOADED) {
            notifyUserAndRestartApp()
        }
    }

    private fun notifyUserAndRestartApp() {
        appUpdateManager.completeUpdate()
            .addOnSuccessListener {
                kiosk.exit()
                startActivity(Intent(this, MainActivityKiosk::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
            }
    }

    override fun onResume() {
        super.onResume()
        kiosk.enter()
        resetIdleTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(this)
    }

    override fun exitKiosk() {
        kiosk.exit()
    }

    override fun checkForUpdate() {
        checkUpdate()
    }

    override fun openWifi() {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetIdleTimer()
    }

    override fun onPause() {
        super.onPause()
        removeCallbacks()
    }

    private fun resetIdleTimer() {
        if (isKiosk()) {
            removeCallbacks()
            handler.postDelayed(idleRunnable, idleTimeout)
        }
    }

    private fun showScreensaver() {
        checkUpdate()
        AppComponent.screensaverHandler.invoke()
    }

    private fun removeCallbacks() {
        if (isKiosk()) {
            handler.removeCallbacks(idleRunnable)
        }
    }
}
