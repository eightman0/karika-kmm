package karika.distribucija.ba.provision

import android.app.Activity
import android.os.Bundle

class ProvisioningSuccessActivity : Activity() {
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        val task = PostProvisioningTask(this)
        if (task.performPostProvisioningOperations()) {
            finish()
            return
        }

        setResult(RESULT_OK)
        finish()
    }
}
