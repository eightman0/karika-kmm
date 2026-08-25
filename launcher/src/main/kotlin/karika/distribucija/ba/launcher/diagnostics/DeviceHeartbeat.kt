package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import karika.distribucija.ba.launcher.update.DashboardApi
import kotlinx.coroutines.tasks.await

/**
 * Reports what's installed so the admin dashboard's device list has something to show - without
 * this, a device only shows up once someone requests a log pull. Called from UpdateWorker on
 * every check (periodic + push-triggered), so freshness matches that cadence.
 */
object DeviceHeartbeat {
    private const val TAG = "DeviceHeartbeat"

    suspend fun report(context: Context, packageName: String, versionCode: Long, versionName: String) {
        try {
            val deviceId = DeviceIdentity.id(context)
            val fcmToken = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
            DashboardApi.reportHeartbeat(
                deviceId = deviceId,
                installedPackage = packageName,
                installedVersionCode = versionCode,
                installedVersionName = versionName,
                androidSdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE,
                deviceModel = Build.MODEL,
                fcmToken = fcmToken
            )
        } catch (e: Exception) {
            Log.w(TAG, "Heartbeat failed: ${e.message}")
        }
    }
}
