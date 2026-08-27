package karika.distribucija.ba.salesrep.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import karika.distribucija.ba.salesrep.MainActivity
import karika.distribucija.ba.salesrep.R
import kotlin.random.Random

/** Mirrors composeApp's KarikaMessagingService.kt. Tapping the resulting notification opens
 * MainActivity with a "route" extra, which it resolves via PushRouteResolver (see
 * MainActivity.handlePushRoute()) into the same destinations the in-app Notifications list uses. */
class KarikaFcmService : FirebaseMessagingService() {

    // No re-registration on rotation - mirrors composeApp's KarikaMessagingService.onNewToken(),
    // which is deliberately a no-op there too (the token is only (re)registered on fresh login).
    override fun onNewToken(token: String) {}

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let { notification ->
            sendNotification(notification, remoteMessage.data)
        }
    }

    private fun sendNotification(notification: RemoteMessage.Notification, data: Map<String, String>) {
        data["route"]?.let { route ->
            (PushRouteResolver.resolve(route) as? NotificationDestination.Conversation)?.let {
                MessagePushBus.publish(it.threadId, it.admin)
            }
        }

        val nextInt = Random.nextInt()
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("route", data["route"])
        }
        val pendingIntent = PendingIntent.getActivity(
            this, nextInt, intent, FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "karika_salesrep_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setSmallIcon(R.drawable.ic_notifications)
            .setColor(applicationContext.getColor(R.color.karika_blue))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(channelId, "Karika obavještenja", IMPORTANCE_DEFAULT)
        )
        manager.notify(nextInt, notificationBuilder.build())
    }
}
