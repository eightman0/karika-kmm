package karika.distribucija.ba.salesrep.diagnostics

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import karika.distribucija.ba.salesrep.R
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
 *
 * Deliberately asks for ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION only, never
 * ACCESS_BACKGROUND_LOCATION - silently granting that one through Device Policy Manager for a
 * package other than the launcher itself was crashing Permission Controller on a real device
 * (which then froze the lock-task-pinned launcher's input, showing up there as an ANR). A brief
 * foreground service, same trick UpdateWorker/LauncherSelfUpdateWorker already use to dodge the
 * cached-app freezer, gets treated as foreground for location access too, so plain
 * ACCESS_FINE_LOCATION is enough even though WorkManager runs this without salesrep visibly in
 * front.
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

        setForeground(createForegroundInfo())

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

    private fun createForegroundInfo(): ForegroundInfo {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, "Lokacija", NotificationManager.IMPORTANCE_MIN
        )
        applicationContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()
        return ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
    }

    private companion object {
        const val NOTIFICATION_CHANNEL_ID = "location_sample_in_progress"
        const val NOTIFICATION_ID = 3
    }
}
