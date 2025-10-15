package karika.distribucija.ba

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class UpdateCompletionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            Log.i(
                "UpdateReceiver",
                "Ažuriranje paketa završeno. Ponovno pokrećem Kiosk aplikaciju."
            )

            val launchIntent = Intent(context, MainActivityKiosk::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }

            context.startActivity(launchIntent)
        }
    }
}