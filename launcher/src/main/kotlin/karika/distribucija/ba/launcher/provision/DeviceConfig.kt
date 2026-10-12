package karika.distribucija.ba.launcher.provision

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.SharedPreferences
import android.os.PersistableBundle
import karika.distribucija.ba.launcher.update.EnrollmentWorker
import karika.distribucija.ba.launcher.update.UpdateScheduler
import karika.distribucija.ba.logging.AppLogger

/**
 * What this device needs to talk to the device API: app/enrollment_token/api_base_url read once
 * from the QR provisioning payload's admin extras bundle (only available at provisioning time),
 * then the device token POST /enroll hands back. Secrets go through SecureStore; the rest is plain
 * SharedPreferences.
 *
 * A device is enrolled once it holds a device token and nothing has since told it the token is
 * gone. A terminal enroll error (bad/expired/used-up QR) or a 401 device_token_invalid on any later
 * call puts it in the re-provisioning state instead: periodic API work stops and LauncherActivity
 * says a new QR scan is needed. Only a factory reset + new QR gets it out of there.
 */
object DeviceConfig {
    private const val PREFS = "device_config"
    private const val KEY_APP = "app"
    private const val KEY_API_BASE_URL = "api_base_url"
    private const val KEY_REPROVISION_REASON = "reprovision_reason"

    private const val SECRET_ENROLLMENT_TOKEN = "enrollment_token"
    private const val SECRET_DEVICE_TOKEN = "device_token"

    enum class ReprovisionReason {
        /** Enroll answered enrollment_token_invalid/expired/exhausted - "QR nije važeći". */
        QR_INVALID,

        /** A bearer call answered device_token_invalid - the device was deleted in Karika Ops, or
         * re-enrolled elsewhere with the same ANDROID_ID. */
        DEVICE_REMOVED,
    }

    /** Which KnownApps entry this device runs (see KnownApps.primary()), null if the QR had none. */
    fun app(context: Context): String? = prefs(context).getString(KEY_APP, null)

    /** No trailing slash, already including /api/V1/device-management. */
    fun apiBaseUrl(context: Context): String? = prefs(context).getString(KEY_API_BASE_URL, null)

    fun enrollmentToken(context: Context): String? = SecureStore.get(context, SECRET_ENROLLMENT_TOKEN)

    fun deviceToken(context: Context): String? = SecureStore.get(context, SECRET_DEVICE_TOKEN)

    fun reprovisionReason(context: Context): ReprovisionReason? =
        prefs(context).getString(KEY_REPROVISION_REASON, null)
            ?.let { runCatching { ReprovisionReason.valueOf(it) }.getOrNull() }

    fun isEnrolled(context: Context): Boolean =
        reprovisionReason(context) == null && apiBaseUrl(context) != null && deviceToken(context) != null

    fun hasPendingEnrollment(context: Context): Boolean =
        reprovisionReason(context) == null && apiBaseUrl(context) != null && enrollmentToken(context) != null

    @SuppressLint("ApplySharedPref")
    fun completeEnrollment(context: Context, deviceToken: String, apiBaseUrl: String, app: String?) {
        SecureStore.put(context, SECRET_DEVICE_TOKEN, deviceToken)
        SecureStore.put(context, SECRET_ENROLLMENT_TOKEN, null)
        prefs(context).edit()
            .putString(KEY_API_BASE_URL, apiBaseUrl.trimEnd('/'))
            .apply { if (app != null) putString(KEY_APP, app) }
            .remove(KEY_REPROVISION_REASON)
            .commit()
    }

    /** Terminal - stops every periodic API call (see UpdateScheduler.cancelDeviceApiWork) and
     * drops whichever token got rejected. */
    @SuppressLint("ApplySharedPref")
    fun requireReprovisioning(context: Context, reason: ReprovisionReason) {
        AppLogger.w(TAG, "Re-provisioning required: $reason")
        when (reason) {
            ReprovisionReason.QR_INVALID -> SecureStore.put(context, SECRET_ENROLLMENT_TOKEN, null)
            ReprovisionReason.DEVICE_REMOVED -> SecureStore.put(context, SECRET_DEVICE_TOKEN, null)
        }
        prefs(context).edit().putString(KEY_REPROVISION_REASON, reason.name).commit()
        UpdateScheduler.cancelDeviceApiWork(context)
    }

    fun addChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs(context).registerOnSharedPreferenceChangeListener(listener)

    fun removeChangeListener(context: Context, listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        prefs(context).unregisterOnSharedPreferenceChangeListener(listener)

    /**
     * Which provisioning checkpoint actually carries EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE in
     * its launching intent is not consistent across Android versions/OEMs - the modern two-phase
     * flow (API 30+) documents ACTION_ADMIN_POLICY_COMPLIANCE (FinalizeActivity) as the place,
     * while the older ACTION_PROVISIONING_SUCCESSFUL (ProvisioningSuccessActivity) carried it on
     * pre-modern flows and may still on some ROMs. Call this from both; whichever one's intent
     * actually has it wins; the other just no-ops on a null bundle. Safe to call twice with the
     * same values if both do - EnrollmentWorker.enqueue() keeps an already-pending enroll.
     */
    @Suppress("DEPRECATION")
    @SuppressLint("ApplySharedPref")
    fun readFromProvisioningIntent(activity: Activity) {
        val checkpoint = activity.javaClass.simpleName
        val extraKeys = activity.intent.extras?.keySet()?.joinToString() ?: "(none)"
        AppLogger.i(TAG, "readFromProvisioningIntent from $checkpoint, intent extras: $extraKeys")

        val extras = activity.intent.getParcelableExtra<PersistableBundle>(
            DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
        )
        if (extras == null) {
            AppLogger.i(TAG, "$checkpoint: no admin extras bundle in this intent")
            return
        }
        val app = extras.getString("app")
        val apiBaseUrl = extras.getString("api_base_url")?.trimEnd('/')
        val enrollmentToken = extras.getString("enrollment_token")
        // The token itself is never logged, only whether it was there.
        AppLogger.i(
            TAG,
            "$checkpoint: admin extras bundle present, app=$app api_base_url=$apiBaseUrl " +
                "enrollment_token=${if (enrollmentToken.isNullOrBlank()) "missing" else "present"}"
        )

        // Must land before LauncherActivity first resumes (both callers start it only afterwards):
        // the app choice is what lets UpdateWorker do its first-ever install, and the pending
        // enrollment is what LauncherActivity shows instead of the re-provisioning screen.
        prefs(activity).edit()
            .apply { if (app != null) putString(KEY_APP, app) }
            .apply { if (apiBaseUrl != null) putString(KEY_API_BASE_URL, apiBaseUrl) }
            .remove(KEY_REPROVISION_REASON)
            .commit()
        if (apiBaseUrl == null || enrollmentToken.isNullOrBlank()) {
            AppLogger.w(TAG, "$checkpoint: QR has no api_base_url/enrollment_token, cannot enroll")
            return
        }
        SecureStore.put(activity, SECRET_ENROLLMENT_TOKEN, enrollmentToken)
        EnrollmentWorker.enqueue(activity)
        AppLogger.i(TAG, "$checkpoint: saved locally and enqueued EnrollmentWorker")
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private const val TAG = "DeviceConfig"
}
