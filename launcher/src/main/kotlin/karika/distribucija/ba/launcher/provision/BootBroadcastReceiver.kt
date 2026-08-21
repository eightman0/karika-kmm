package karika.distribucija.ba.launcher.provision

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import karika.distribucija.ba.launcher.LauncherActivity

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val launchIntent = Intent(context, LauncherActivity::class.java)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(launchIntent)
        }
    }
}
