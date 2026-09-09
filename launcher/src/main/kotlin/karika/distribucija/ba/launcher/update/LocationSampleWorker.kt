package karika.distribucija.ba.launcher.update

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import karika.distribucija.ba.launcher.diagnostics.LocationHistoryStore
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Samples one GPS fix roughly every 15 min (WorkManager's own floor for periodic work) and
 * appends it to LocationHistoryStore - never touches the network itself, DeviceHeartbeat flushes
 * the accumulated queue on every heartbeat instead.
 */
class LocationSampleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Not granted yet - e.g. this fires before LauncherActivity's first onResume has had
            // a chance to apply kiosk policies. Try again next cycle rather than erroring loudly.
            return Result.success()
        }

        val client = LocationServices.getFusedLocationProviderClient(applicationContext)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        val location = runCatching {
            client.getCurrentLocation(request, CancellationTokenSource().token).await()
        }.getOrNull() ?: return Result.success()

        val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        LocationHistoryStore.add(applicationContext, location.latitude, location.longitude, timestamp)
        return Result.success()
    }
}
