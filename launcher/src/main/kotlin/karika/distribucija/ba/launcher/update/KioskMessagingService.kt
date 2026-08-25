package karika.distribucija.ba.launcher.update

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import karika.distribucija.ba.launcher.diagnostics.LogUploadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Delivers pushes the admin dashboard sends: an update-check nudge on publish, or a log-pull
 * request. Data-only messages (no `notification` payload), so Play Services wakes this process to
 * hand them to onMessageReceived() even if the process was frozen or not running - the FCM
 * connection lives in Play Services, not in our own process, so it isn't subject to the same
 * cached-app freezing our process is. */
class KioskMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        when (message.data["type"]) {
            TYPE_LOG_REQUEST -> scope.launch {
                LogUploadManager.uploadNow(applicationContext, message.data["requestedAt"])
            }
            else -> UpdateScheduler.triggerImmediateCheck(applicationContext)
        }
    }

    companion object {
        const val BROADCAST_TOPIC = "kiosk-updates"
        private const val TYPE_LOG_REQUEST = "log_request"

        fun deviceTopic(deviceId: String) = "device_$deviceId"
    }
}
