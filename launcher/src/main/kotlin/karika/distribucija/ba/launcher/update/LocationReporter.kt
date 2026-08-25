package karika.distribucija.ba.launcher.update

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.concurrent.Executors
import kotlin.coroutines.resume

/** Fetches a one-shot location fix and reports it to the dashboard - triggered by an FCM push
 * (KioskMessagingService), same channel as the log-pull request. ACCESS_FINE_LOCATION is granted
 * silently via DevicePolicyManager (LauncherKiosk), so there is no runtime prompt to answer. */
object LocationReporter {
    private const val TAG = "LocationReporter"
    private const val TIMEOUT_MS = 20_000L

    suspend fun reportNow(context: Context) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Location permission not granted, skipping")
                return
            }
            val location = fetchLocation(context) ?: return
            DashboardApi.reportLocation(
                DeviceIdentity.id(context),
                location.latitude,
                location.longitude,
                location.accuracy
            )
        } catch (e: Exception) {
            Log.e(TAG, "Location report failed", e)
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
