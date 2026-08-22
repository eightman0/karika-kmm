package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

/**
 * Reports what's installed so the admin dashboard's device list has something to show - without
 * this, devices/{deviceId} only exists once someone requests a log pull. Called from
 * UpdateWorker on every check (periodic + real-time-triggered), so freshness matches that cadence.
 */
object DeviceHeartbeat {
    private const val TAG = "DeviceHeartbeat"

    suspend fun report(context: Context, packageName: String, versionCode: Long, versionName: String) {
        try {
            if (Firebase.auth.currentUser == null) {
                Firebase.auth.signInAnonymously().await()
            }
            val deviceId = DeviceIdentity.id(context)
            Firebase.firestore.collection("devices").document(deviceId)
                .set(
                    mapOf(
                        "authUid" to Firebase.auth.currentUser?.uid,
                        "installedPackage" to packageName,
                        "installedVersionCode" to versionCode,
                        "installedVersionName" to versionName,
                        "androidSdkInt" to Build.VERSION.SDK_INT,
                        "androidRelease" to Build.VERSION.RELEASE,
                        "deviceModel" to Build.MODEL,
                        "lastSeenAt" to Timestamp.now()
                    ),
                    SetOptions.merge()
                ).await()
        } catch (e: Exception) {
            Log.w(TAG, "Heartbeat failed: ${e.message}")
        }
    }
}
