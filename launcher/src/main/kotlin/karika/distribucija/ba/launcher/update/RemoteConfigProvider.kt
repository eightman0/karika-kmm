package karika.distribucija.ba.launcher.update

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Backs the silent-update mechanism for the payload app (salesrep): the admin publishes a new
 * [KioskVersion] by editing these parameters in the Firebase console (or via the Remote Config
 * REST API), and every device with an active [ConfigUpdateListener] picks it up in real time - no
 * per-device push token bookkeeping needed. [MIN_FETCH_INTERVAL_SECONDS] is only the fallback
 * poll cadence for when a device's real-time stream isn't connected (killed process, no listener
 * yet, transient GMS hiccup).
 */
object RemoteConfigProvider {
    private const val TAG = "RemoteConfigProvider"

    private const val KEY_VERSION_CODE = "kiosk_version_code"
    private const val KEY_VERSION_NAME = "kiosk_version_name"
    private const val KEY_APK_URL = "kiosk_apk_url"
    private const val KEY_APK_SHA256 = "kiosk_apk_sha256"
    private const val KEY_MANDATORY = "kiosk_mandatory"

    private const val MIN_FETCH_INTERVAL_SECONDS = 3600L

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings { minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SECONDS }
            )
            setDefaultsAsync(
                mapOf(
                    KEY_VERSION_CODE to 0L,
                    KEY_VERSION_NAME to "",
                    KEY_APK_URL to "",
                    KEY_APK_SHA256 to "",
                    KEY_MANDATORY to false
                )
            )
        }
    }

    private var listening = false

    /** Call once from Application.onCreate(): does an initial fetch and opens the real-time stream. */
    fun init(context: Context) {
        val appContext = context.applicationContext
        remoteConfig.fetchAndActivate()
        startRealtimeListener(appContext)
    }

    private fun startRealtimeListener(context: Context) {
        if (listening) return
        listening = true
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                remoteConfig.activate()
                UpdateScheduler.triggerImmediateCheck(context)
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.w(TAG, "Remote Config real-time listener error, falling back to periodic fetch", error)
            }
        })
    }

    /** Explicit fetch for the periodic update-check worker; returns true if new values were activated. */
    suspend fun fetchLatest(): Boolean = remoteConfig.fetchAndActivate().await()

    fun latestVersion(): KioskVersion = KioskVersion(
        versionCode = remoteConfig.getLong(KEY_VERSION_CODE),
        versionName = remoteConfig.getString(KEY_VERSION_NAME),
        apkUrl = remoteConfig.getString(KEY_APK_URL),
        apkSha256 = remoteConfig.getString(KEY_APK_SHA256),
        mandatory = remoteConfig.getBoolean(KEY_MANDATORY)
    )

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            val exception = task.exception
            if (exception != null) {
                cont.cancel(exception)
            } else {
                cont.resumeWith(Result.success(task.result))
            }
        }
    }
}
