package karika.distribucija.ba.launcher.diagnostics

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import karika.distribucija.ba.launcher.R

/**
 * Runs as a foreground service for the launcher's whole lifetime so the process stays out of
 * Android's cached-app freezer while salesrep is on top (its normal, near-permanent state) -
 * without this, the OS suspends the process entirely, including the Remote Config and Firestore
 * real-time listener threads, so a published update or a log-pull request can sit unprocessed
 * for however long the device happens to stay frozen instead of arriving in real time.
 */
class KeepAliveService : Service() {

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Kiosk pozadinski servis", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "keep_alive"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, KeepAliveService::class.java))
        }
    }
}
