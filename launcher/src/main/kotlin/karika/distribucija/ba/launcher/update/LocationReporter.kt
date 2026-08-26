package karika.distribucija.ba.launcher.update

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import karika.distribucija.ba.launcher.LauncherActivity
import karika.distribucija.ba.launcher.MaintenanceState
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/**
 * Fetches a one-shot location fix and reports it to the dashboard - triggered by the
 * location_request FCM command (KioskMessagingService, already running on a background
 * dispatcher via runAcked). Brings the launcher to the front behind the maintenance banner for
 * the duration of the fetch, same mechanism UpdateWorker uses during an install, so salesrep
 * automatically resumes the moment the fetch finishes (success, failure, or timeout - the finally
 * block covers all three) instead of needing a separate "done" signal.
 *
 * ACCESS_FINE_LOCATION is granted via DevicePolicyManager the first time this actually runs, not
 * unconditionally on every LauncherKiosk.enter() the way an earlier attempt at this feature did
 * (see git history: "Add per-device and fleet-wide geolocation requests", reverted 18 minutes
 * later) - that version called setPermissionGrantState() from LauncherActivity.onResume(), i.e.
 * the main thread, on every single resume, and this codebase has since seen the exact same
 * DevicePolicyManager.setPermissionGrantState() call for a different dangerous permission
 * (POST_NOTIFICATIONS) cause a real-device ANR when called the same way. Doing it here instead -
 * lazily, at most once ever, from runAcked's background dispatcher rather than onResume's main
 * thread - should avoid the specific mechanism (a blocked main thread tripping Android's ANR
 * watchdog) even if the underlying binder call itself is genuinely slow on this ROM. Not yet
 * verified on real hardware; if it reproduces the crash anyway, drop this for a runtime prompt
 * answered once during provisioning instead.
 */
object LocationReporter {
    private const val TAG = "LocationReporter"
    private const val TIMEOUT_MS = 20_000L

    suspend fun reportNow(context: Context) {
        val appContext = context.applicationContext
        MaintenanceState.begin(appContext)
        LauncherActivity.bringToFront(appContext)
        try {
            ensurePermission(appContext)
            val location = fetchLocation(appContext)
            if (location != null) {
                DashboardApi.reportLocation(
                    DeviceIdentity.id(appContext),
                    location.latitude,
                    location.longitude,
                    location.accuracy
                )
            } else {
                Log.w(TAG, "No location fix obtained")
            }
        } finally {
            MaintenanceState.end(appContext)
        }
    }

    private fun ensurePermission(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) return
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(context)
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).forEach {
            runCatching {
                devicePolicyManager.setPermissionGrantState(
                    admin, context.packageName, it, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                )
            }.onFailure { e -> Log.e(TAG, "Failed to grant $it", e) }
        }
    }

    private suspend fun fetchLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = bestProvider(locationManager) ?: run {
            Log.w(TAG, "No enabled location provider")
            return null
        }
        return try {
            withTimeout(TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    val signal = CancellationSignal()
                    cont.invokeOnCancellation { signal.cancel() }
                    locationManager.getCurrentLocation(provider, signal, Executors.newSingleThreadExecutor()) { location ->
                        if (cont.isActive) cont.resume(location)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.w(TAG, "Location fetch timed out")
            null
        }
    }

    /** FUSED_PROVIDER (best accuracy, merges GPS+network) only exists from API 31 - this app's
     * minSdk is 30, so it needs a fallback path for that one version. */
    private fun bestProvider(locationManager: LocationManager): String? {
        val candidates = if (Build.VERSION.SDK_INT >= 31) {
            listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        } else {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        }
        return candidates.firstOrNull { runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false) }
    }
}
