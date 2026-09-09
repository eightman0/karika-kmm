package karika.distribucija.ba.salesrep.diagnostics

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
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Samples one GPS fix roughly every 15 min (WorkManager's own floor for periodic work) and
 * appends it to LocationHistoryStore. Runs here (moved from the launcher) because sampling from
 * that Device Owner background process was hitting ANRs on real devices - this app is normally
 * the one actually in the foreground. Never touches the network itself - the launcher's heartbeat
 * pulls the accumulated file on its own schedule, through the same content-URI bridge as logs.
 */
class LocationSampleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Not granted yet - e.g. this fires before the launcher's Device Owner policies have
            // had a chance to apply. Try again next cycle rather than erroring loudly.
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
        LocationHistoryStore.add(location.latitude, location.longitude, timestamp)
        return Result.success()
    }
}
