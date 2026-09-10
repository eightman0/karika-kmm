package karika.distribucija.ba.launcher.provision

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import karika.distribucija.ba.launcher.LauncherActivity
import karika.distribucija.ba.launcher.update.LauncherSelfUpdateWorker
import karika.distribucija.ba.logging.AppLogger

/**
 * Three chances to relaunch, not one: LOCKED_BOOT_COMPLETED fires earliest but races credential-
 * encrypted storage unlock - LauncherActivity/LauncherApp aren't direct-boot-aware, so that early
 * start can get silently dropped depending on boot timing. BOOT_COMPLETED fires later, well after
 * unlock, with no such race - it's the reliable backstop if the early attempt didn't land.
 *
 * MY_PACKAGE_REPLACED covers a third case that isn't a boot at all: LauncherSelfUpdateWorker
 * silently installing an update to this app's own running package. That kills this process as
 * part of applying the update, and on a real device the system was landing on the recent-apps
 * screen afterward instead of bringing the (now updated) launcher back - lock task mode had
 * already ended along with the killed process, and nothing was left running to re-request it.
 * MY_PACKAGE_REPLACED is exactly the broadcast Android sends for this: the system starts a fresh
 * process specifically to deliver it, once the update is already fully applied, so relaunching
 * here doesn't depend on anything from the old (now-gone) process surviving past its own install.
 *
 * The ongoing "Ažuriranje" foreground-service notification (LauncherSelfUpdateWorker) never gets a
 * chance to be dismissed normally either - its process dies the same abrupt way, mid-service, with
 * no stopForeground() call. A real device was seen with the notification shade left showing
 * afterward, on top of the recents screen from the paragraph above - cancelling it here, on the
 * same MY_PACKAGE_REPLACED path, covers that regardless of whether the system already cleared it
 * on its own (cancelling an already-gone notification id is a no-op).
 */
class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Logged via AppLogger, not just Log - this is the one thing standing between the
                // launcher self-update killing its own process and the device coming back up, so a
                // remote log pull (no ADB needed) needs to be able to show whether this branch even
                // ran, not just that the process eventually did something.
                AppLogger.i(TAG, "Received ${intent.action}, bringing launcher to front")
                context?.let {
                    it.getSystemService(NotificationManager::class.java)
                        ?.cancel(LauncherSelfUpdateWorker.NOTIFICATION_ID)
                    LauncherActivity.bringToFront(it)
                }
            }
        }
    }

    private companion object {
        const val TAG = "BootBroadcastReceiver"
    }
}
