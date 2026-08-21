package karika.distribucija.ba.launcher.provision

import android.app.Activity
import android.os.Bundle

class FinalizeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_OK)
        finish()
    }
}
