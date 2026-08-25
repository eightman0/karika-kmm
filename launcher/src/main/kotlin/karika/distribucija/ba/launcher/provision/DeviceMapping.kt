package karika.distribucija.ba.launcher.provision

import android.content.Context

/** customer_id/site_id read once from the QR provisioning payload's admin extras bundle (see
 * ProvisioningSuccessActivity) and persisted locally so later code (heartbeat, dashboard reports)
 * doesn't need to re-parse provisioning extras, which are only available at provisioning time. */
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
}
