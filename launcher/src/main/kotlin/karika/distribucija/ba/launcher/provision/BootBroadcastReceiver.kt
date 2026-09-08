package karika.distribucija.ba.launcher.provision

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import karika.distribucija.ba.launcher.LauncherActivity

/**
 * Two chances to relaunch, not one: LOCKED_BOOT_COMPLETED fires earliest but races credential-
 * encrypted storage unlock - LauncherActivity/LauncherApp aren't direct-boot-aware, so that early
 * start can get silently dropped depending on boot timing. BOOT_COMPLETED fires later, well after
 * unlock, with no such race - it's the reliable backstop if the early attempt didn't land.
 */
class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_BOOT_COMPLETED -> {
                val launchIntent = Intent(context, LauncherActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startActivity(launchIntent)
            }
        }
    }
}
