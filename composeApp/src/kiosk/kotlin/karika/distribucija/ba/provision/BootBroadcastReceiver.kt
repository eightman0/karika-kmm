package karika.distribucija.ba.provision

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import karika.distribucija.ba.MainActivityKiosk


class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val launchIntent = Intent(context, MainActivityKiosk::class.java)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(launchIntent)
        }
    }
}