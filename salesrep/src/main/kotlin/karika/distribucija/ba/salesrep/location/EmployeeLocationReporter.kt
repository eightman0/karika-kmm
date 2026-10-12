package karika.distribucija.ba.salesrep.location

import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import karika.distribucija.ba.salesrep.SalesRepApp
import karika.distribucija.ba.salesrep.api.EmployeeLocationRepository
import karika.distribucija.ba.salesrep.model.EmployeeLocationSubmit
import karika.distribucija.ba.salesrep.model.ResultState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Mirrors composeApp's hourly employee-location submission (see
 * karika.distribucija.ba.domain.model.EmployeeLocation) for this standalone native app: fetches
 * one GPS fix an hour and posts it to the same Magento endpoint.
 *
 * Gated on [ProcessLifecycleOwner] (app in foreground), not plain process-alive, because a
 * location request from a backgrounded process silently returns nothing without
 * ACCESS_BACKGROUND_LOCATION - a permission this app deliberately never requests (see
 * LocationSampleWorker's own comment on why that crashes Permission Controller on real devices).
 *
 * Checks once a minute and sends when one is due: right after login (composeApp sends as soon as
 * the sales rep dashboard opens) and then an hour after the last successful send. Starting in
 * Application.onCreate used to fire the first attempt before login and before the permission
 * dialog was answered, so it failed and the next try came only an hour later.
 */
object EmployeeLocationReporter {
    private val repository = EmployeeLocationRepository()
    private val INTERVAL = 1.hours
    private val CHECK_EVERY = 1.minutes

    /** elapsedRealtime of the last successful send; 0 = never in this session (send ASAP). */
    @Volatile
    private var lastSentAt = 0L

    fun start(context: Context) {
        val appContext = context.applicationContext
        val owner = ProcessLifecycleOwner.get()
        owner.lifecycleScope.launch {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    if (!isLoggedIn(appContext)) {
                        // Logged out: the next login gets an immediate send.
                        lastSentAt = 0L
                    } else if (isDue()) {
                        runCatching { submitCurrentLocation(appContext) }
                    }
                    delay(CHECK_EVERY)
                }
            }
        }
    }

    private fun isDue(): Boolean =
        lastSentAt == 0L || SystemClock.elapsedRealtime() - lastSentAt >= INTERVAL.inWholeMilliseconds

    private fun isLoggedIn(context: Context): Boolean =
        (context as? SalesRepApp)?.let { it.isSessionReady() && it.sessionManager.hasToken() } == true

    private suspend fun submitCurrentLocation(context: Context) {
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        val location = runCatching {
            client.getCurrentLocation(request, CancellationTokenSource().token).await()
        }.getOrNull() ?: return

        repository.submit(
            EmployeeLocationSubmit(
                deviceId = DeviceIdentifier.deviceId(context),
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
                altitude = if (location.hasAltitude()) location.altitude else null,
                speed = if (location.hasSpeed()) location.speed.toDouble() else null,
                heading = if (location.hasBearing()) location.bearing.toDouble() else null,
                timestamp = Instant.now().toString(),
            )
        ).collect { result ->
            if (result is ResultState.Success) lastSentAt = SystemClock.elapsedRealtime()
        }
    }
}
