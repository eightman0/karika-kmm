package karika.distribucija.ba.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import karika.distribucija.ba.AppComponent
import karika.distribucija.ba.KarikaActivity
import karika.distribucija.ba.R
import kotlin.random.Random

class KarikaMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            sendNotification(notification, remoteMessage.data)
        }
    }

    private fun sendNotification(
        notification: RemoteMessage.Notification,
        data: MutableMap<String, String>
    ) {
        AppComponent.refreshHandler.invoke()
        val nextInt = Random.nextInt()
        val intent = Intent(this, KarikaActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            )
            putExtra("route", data["route"])
        }

        val pendingIntent = PendingIntent.getActivity(
            this, nextInt, intent, FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "KarikaNotificationChannelId"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(applicationContext.getColor(R.color.karika))
            .setAutoCancel(true)
            .setSound(Uri.parse("android.resource://${applicationContext.packageName}/raw/android_ta_da"))
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "KarikaChannel", IMPORTANCE_DEFAULT)
            )
        }
        manager.notify(nextInt, notificationBuilder.build())
    }
}