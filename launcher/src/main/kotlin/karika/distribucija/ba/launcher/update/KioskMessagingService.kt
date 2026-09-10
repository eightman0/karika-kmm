package karika.distribucija.ba.launcher.update

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import karika.distribucija.ba.launcher.LauncherActivity
import karika.distribucija.ba.launcher.RemoteDebugUnlock
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
 * reboot, launcher self-update, temporary debug unlock).
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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val command = message.data["command"] ?: message.data["type"]
        val requestId = message.data["requestId"]
        Log.i(TAG, "Command received: $command requestId=$requestId")

        // Every push is also a chance to report current status (version, maintenance, battery,
        // any queued GPS fixes) - cheap since it's just a WorkManager enqueue, deduped against
        // any other pending trigger via ExistingWorkPolicy.REPLACE, so this can't pile up.
        UpdateScheduler.triggerImmediateCheck(applicationContext)

        when (command) {
            CMD_LOG_REQUEST -> runAcked(command, requestId) {
                LogUploadManager.uploadNow(applicationContext, message.data["requestedAt"])
            }
            CMD_ANALYTICS_REQUEST -> runAcked(command, requestId) {
                LogUploadManager.uploadAnalyticsNow(applicationContext)
            }
            CMD_FACTORY_RESET -> scope.launch {
                // Acked before wiping, not after - same reasoning as CMD_REBOOT below, wipeData()
                // can bring the device down before an ack sent afterward would ever complete.
                ack(command, requestId, "ok", null)
                val devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
                devicePolicyManager.wipeData(0)
            }
            CMD_REBOOT -> scope.launch {
                // Acked before rebooting, not after like every other command - unlike factory
                // reset (which wipes this device_id's history along with everything else),
                // reboot leaves the same device_id behind expecting to see this in its history,
                // but devicePolicyManager.reboot() can bring the device down before an ack sent
                // afterward would ever finish its network round trip.
                ack(command, requestId, "ok", null)
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
            CMD_UPDATE_LAUNCHER -> scope.launch {
                // Acked before installing, not after - same reasoning as CMD_REBOOT above:
                // installing an update to this process's own package gets it killed and replaced
                // by the system before an ack sent afterward would reliably go out.
                ack(command, requestId, "ok", null)
                UpdateScheduler.triggerLauncherSelfUpdate(applicationContext)
            }
            CMD_DEBUG_UNLOCK -> runAcked(command, requestId) {
                RemoteDebugUnlock.begin(applicationContext)
                // Setting the flag alone only takes effect the next time LauncherActivity resumes
                // on its own - could be indefinite with salesrep sitting in front. Force lock task
                // off immediately: removing the current package from the allowlist is documented
                // to stop lock task mode right away, regardless of which activity holds the pin.
                val devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
                val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(applicationContext)
                devicePolicyManager.setLockTaskPackages(admin, arrayOf())
            }
            CMD_DEBUG_LOCK -> runAcked(command, requestId) {
                RemoteDebugUnlock.end(applicationContext)
                LauncherActivity.bringToFront(applicationContext)
            }
            // The immediate heartbeat triggered above is the whole point of both of these - the
            // ack just confirms the push itself was received (and, for version_check, which
            // version was targeted).
            CMD_PING -> runAcked(command, requestId) {}
            CMD_VERSION_CHECK -> runAcked(command, requestId, message.data["versionCode"]?.let { "verzija $it" }) {}
            // No "command" field at all - the old Remote Config real-time-listener push, kept for
            // any straggler still wired to it. Nothing further to do: the trigger above already ran.
            null -> {}
            else -> Log.w(TAG, "Unknown command: $command")
        }
    }

    private fun runAcked(
        command: String,
        requestId: String?,
        okMessage: String? = null,
        block: suspend () -> Unit
    ) {
        scope.launch {
            try {
                block()
                ack(command, requestId, "ok", okMessage)
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
        private const val CMD_UPDATE_LAUNCHER = "update_launcher"
        private const val CMD_DEBUG_UNLOCK = "debug_unlock"
        private const val CMD_DEBUG_LOCK = "debug_lock"

        fun deviceTopic(deviceId: String) = "device_$deviceId"
    }
}
