package karika.distribucija.ba.launcher.provision

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Bundle

@SuppressLint("NewApi")
class GetProvisioningModeActivity : Activity() {
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        val intent = Intent()
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_MODE,
            DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
        )
        setResult(RESULT_OK, intent)
        finish()
    }
}
