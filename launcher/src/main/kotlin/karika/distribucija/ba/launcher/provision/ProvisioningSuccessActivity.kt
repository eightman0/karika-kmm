package karika.distribucija.ba.launcher.provision

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import karika.distribucija.ba.launcher.LauncherActivity

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

        startActivity(
            Intent(this, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )

        setResult(RESULT_OK)
        finish()
    }
}
