package karika.distribucija.ba.launcher.update

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/** Delivers the "check now" push the admin dashboard sends on publish. Data-only message (no
 * `notification` payload), so Play Services wakes this process to hand it to onMessageReceived()
 * even if the process was frozen or not running - the FCM connection lives in Play Services, not
 * in our own process, so it isn't subject to the same cached-app freezing our process is. */
class KioskMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        UpdateScheduler.triggerImmediateCheck(applicationContext)
    }

    companion object {
        const val TOPIC = "kiosk-updates"
    }
}
