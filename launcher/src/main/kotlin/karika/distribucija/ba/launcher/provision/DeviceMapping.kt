package karika.distribucija.ba.launcher.provision

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.PersistableBundle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import karika.distribucija.ba.launcher.update.DeviceMappingWorker

/** customer_id/site_id read once from the QR provisioning payload's admin extras bundle and
 * persisted locally so later code (heartbeat, dashboard reports) doesn't need to re-parse
 * provisioning extras, which are only available at provisioning time. */
object DeviceMapping {
    private const val PREFS = "device_mapping"
    private const val KEY_CUSTOMER_ID = "customer_id"
    private const val KEY_SITE_ID = "site_id"

    fun save(context: Context, customerId: String?, siteId: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_CUSTOMER_ID, customerId)
            .putString(KEY_SITE_ID, siteId)
            .apply()
    }

    fun customerId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_CUSTOMER_ID, null)

    fun siteId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SITE_ID, null)

    /**
     * Which provisioning checkpoint actually carries EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE in
     * its launching intent is not consistent across Android versions/OEMs - the modern two-phase
     * flow (API 30+) documents ACTION_ADMIN_POLICY_COMPLIANCE (FinalizeActivity) as the place,
     * while the older ACTION_PROVISIONING_SUCCESSFUL (ProvisioningSuccessActivity) carried it on
     * pre-modern flows and may still on some ROMs. Call this from both; whichever one's intent
     * actually has it wins; the other just no-ops on a null bundle. Safe to call twice with the
     * same values if both do.
     */
    @Suppress("DEPRECATION")
    fun readFromProvisioningIntent(activity: Activity) {
        val extras = activity.intent.getParcelableExtra<PersistableBundle>(
            DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
        ) ?: return
        val customerId = extras.getString("customer_id")
        val siteId = extras.getString("site_id")
        if (customerId == null && siteId == null) return

        save(activity, customerId, siteId)
        WorkManager.getInstance(activity).enqueue(
            OneTimeWorkRequestBuilder<DeviceMappingWorker>()
                .setInputData(
                    workDataOf(
                        DeviceMappingWorker.KEY_CUSTOMER_ID to customerId,
                        DeviceMappingWorker.KEY_SITE_ID to siteId
                    )
                )
                .build()
        )
    }
}
