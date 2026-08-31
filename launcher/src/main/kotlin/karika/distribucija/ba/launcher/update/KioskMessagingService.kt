package karika.distribucija.ba.launcher.update

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import karika.distribucija.ba.launcher.LauncherActivity
import karika.distribucija.ba.launcher.RemoteMaintenanceState
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import karika.distribucija.ba.launcher.diagnostics.LogUploadManager
import karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Delivers pushes the admin dashboard sends: an update-check nudge on publish, or a device
 * command (log pull, analytics pull, factory reset, maintenance toggle, open settings, ping,
 * reboot).
 * Data-only messages (no `notification` payload), so Play Services wakes this process to hand
 * them to onMessageReceived() even if the process was frozen or not running - the FCM connection
 * lives in Play Services, not in our own process, so it isn't subject to the same cached-app
 * freezing our process is.
 *
 * Every command is logged with its requestId and acked back to the dashboard (best-effort - if
 * the ack itself fails to send, the command still ran; the dashboard just won't show a result)
 * so a command sent from the field has some visibility into whether it actually landed.
 */
class KioskMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val command = message.data["command"] ?: message.data["type"]
        val requestId = message.data["requestId"]
        Log.i(TAG, "Command received: $command requestId=$requestId")

        when (command) {
            CMD_LOG_REQUEST -> runAcked(command, requestId) {
                LogUploadManager.uploadNow(applicationContext, message.data["requestedAt"])
            }
            CMD_ANALYTICS_REQUEST -> runAcked(command, requestId) {
                LogUploadManager.uploadAnalyticsNow(applicationContext)
            }
            CMD_FACTORY_RESET -> runAcked(command, requestId) {
                // The device is about to erase itself, so the ack that follows is moot in
                // practice - kept anyway since runAcked() is the uniform path for every command.
                val devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
                devicePolicyManager.wipeData(0)
            }
            CMD_REBOOT -> runAcked(command, requestId) {
                val devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
                val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(applicationContext)
                devicePolicyManager.reboot(admin)
            }
            CMD_MAINTENANCE_ON -> runAcked(command, requestId) {
                RemoteMaintenanceState.begin(applicationContext)
                // Setting the flag alone only shows up next time LauncherActivity resumes on its
                // own - if salesrep is currently in front, that could be indefinite. Force it.
                LauncherActivity.bringToFront(applicationContext)
            }
            CMD_MAINTENANCE_OFF -> runAcked(command, requestId) {
                RemoteMaintenanceState.end(applicationContext)
            }
            CMD_OPEN_SETTINGS -> runAcked(command, requestId) {
                // com.android.settings is already on LauncherKiosk's lock task allowlist, so this
                // opens on top of whatever's currently in front without needing to leave lock task
                // mode at all - the device stays otherwise locked down the whole time.
                val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(intent)
            }
            CMD_PING -> runAcked(command, requestId) {
                // No actual work - a successful ack is the whole point, proof the device is alive
                // and reachable right now rather than just "was seen at some point in the past".
            }
            CMD_VERSION_CHECK, null -> UpdateScheduler.triggerImmediateCheck(applicationContext)
            else -> Log.w(TAG, "Unknown command: $command")
        }
    }

    private fun runAcked(command: String, requestId: String?, block: suspend () -> Unit) {
        scope.launch {
            try {
                block()
                ack(command, requestId, "ok", null)
            } catch (e: Exception) {
                Log.e(TAG, "Command failed: $command", e)
                ack(command, requestId, "error", e.message)
            }
        }
    }

    private suspend fun ack(command: String, requestId: String?, status: String, message: String?) {
        runCatching {
            DashboardApi.reportCommandAck(
                DeviceIdentity.id(applicationContext), command, requestId, status, message
            )
        }
    }

    companion object {
        private const val TAG = "KioskMessagingService"
        const val BROADCAST_TOPIC = "kiosk-updates"

        private const val CMD_LOG_REQUEST = "log_request"
        private const val CMD_ANALYTICS_REQUEST = "analytics_request"
        private const val CMD_FACTORY_RESET = "factory_reset"
        private const val CMD_REBOOT = "reboot"
        private const val CMD_MAINTENANCE_ON = "maintenance_on"
        private const val CMD_MAINTENANCE_OFF = "maintenance_off"
        private const val CMD_OPEN_SETTINGS = "open_settings"
        private const val CMD_PING = "ping"
        private const val CMD_VERSION_CHECK = "version_check"

        fun deviceTopic(deviceId: String) = "device_$deviceId"
    }
}
