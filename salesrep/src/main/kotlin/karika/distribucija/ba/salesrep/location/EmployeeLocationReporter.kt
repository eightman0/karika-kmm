package karika.distribucija.ba.salesrep.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import karika.distribucija.ba.salesrep.api.EmployeeLocationRepository
import karika.distribucija.ba.salesrep.model.EmployeeLocationSubmit
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import kotlin.time.Duration.Companion.hours

/**
 * Mirrors composeApp's hourly employee-location submission (see
 * karika.distribucija.ba.domain.model.EmployeeLocation) for this standalone native app: fetches
 * one GPS fix an hour and posts it to the same Magento endpoint.
 *
 * Gated on [ProcessLifecycleOwner] (app in foreground), not plain process-alive, because a
 * location request from a backgrounded process silently returns nothing without
 * ACCESS_BACKGROUND_LOCATION - a permission this app deliberately never requests (see
 * LocationSampleWorker's own comment on why that crashes Permission Controller on real devices).
 */
object EmployeeLocationReporter {
    private val repository = EmployeeLocationRepository()

    fun start(context: Context) {
        val appContext = context.applicationContext
        val owner = ProcessLifecycleOwner.get()
        owner.lifecycleScope.launch {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    runCatching { submitCurrentLocation(appContext) }
                    delay(1.hours)
                }
            }
        }
    }

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
        ).collect { }
    }
}
