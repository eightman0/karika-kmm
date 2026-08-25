package karika.distribucija.ba.launcher.provision

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import karika.distribucija.ba.launcher.LauncherActivity
import karika.distribucija.ba.launcher.update.DeviceMappingWorker

/** Reached at the very end of the managed-provisioning flow (ACTION_PROVISIONING_SUCCESSFUL).
 * Nothing else in this flow ever starts the kiosk: onProfileProvisioningComplete() is a no-op on
 * API 26+ (see the SDK_INT check in LauncherDeviceAdminReceiver), and LauncherActivity only
 * registers itself as the persistent preferred HOME app from its own onResume() - so unless
 * something explicitly launches it here, the device is left on whatever the OS falls back to
 * (e.g. the stock launcher) instead of the kiosk. */
class ProvisioningSuccessActivity : Activity() {
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        PostProvisioningTask(this).performPostProvisioningOperations()
        readDeviceMapping()

        startActivity(
            Intent(this, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )

        setResult(RESULT_OK)
        finish()
    }

    /** customer_id/site_id, if the QR JSON's PROVISIONING_ADMIN_EXTRAS_BUNDLE carried them (see
     * provisioning.html on the dashboard for the exact key names) - read once here since
     * provisioning extras aren't available anywhere else afterward. */
    @Suppress("DEPRECATION")
    private fun readDeviceMapping() {
        val extras = intent.getParcelableExtra<PersistableBundle>(
            DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE
        ) ?: return
        val customerId = extras.getString("customer_id")
        val siteId = extras.getString("site_id")
        if (customerId == null && siteId == null) return

        DeviceMapping.save(this, customerId, siteId)
        WorkManager.getInstance(this).enqueue(
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
