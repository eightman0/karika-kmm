package karika.distribucija.ba.launcher.provision

import android.app.Activity
import android.os.Bundle

/** Handles ACTION_ADMIN_POLICY_COMPLIANCE - the modern (API 30+) provisioning checkpoint Android
 * documents as carrying EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, unlike the older
 * ACTION_PROVISIONING_SUCCESSFUL (ProvisioningSuccessActivity) this codebase originally read it
 * from exclusively - which is why customer_id/site_id from the QR never showed up on the
 * dashboard on a real device. See DeviceMapping.readFromProvisioningIntent(). */
class FinalizeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DeviceMapping.readFromProvisioningIntent(this)
        setResult(RESULT_OK)
        finish()
    }
}
