package karika.distribucija.ba

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File

/** Plain payload activity - lock task, Home role and updates are all the launcher's job (it is
 * the Device Owner and installs this app through the admin dashboard), not this app's. */
class MainActivityKiosk : KarikaActivity() {
    private val idleTimeout = 20000L
    private val handler = Handler(Looper.getMainLooper())
    private val idleRunnable = Runnable {
        showScreensaver()
    }
    private var takePhotoCallback: ((String, ByteArray) -> Unit)? = null

    /** CameraCaptureActivity, not an IMAGE_CAPTURE intent - see its own doc comment for why. */
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = takePhotoCallback
            takePhotoCallback = null
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val path = result.data?.getStringExtra(CameraCaptureActivity.EXTRA_RESULT_PATH)
                ?: return@registerForActivityResult
            val file = File(path)
            if (!file.exists()) return@registerForActivityResult
            val bytes = file.readBytes()
            file.delete()
            callback?.invoke(file.name, bytes)
        }

    /** A plain runtime request - the dialog opens in this app's own task, so lock task allows it,
     * same as salesrep's AttachmentPicker. */
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchCamera()
            } else {
                takePhotoCallback = null
                Toast.makeText(this, "Za slikanje je potrebna dozvola za kameru", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun pickFile(mediaTypes: Array<String>, callback: (String, ByteArray) -> Unit) {
        takePhoto(callback)
    }

    override fun takePhoto(callback: (String, ByteArray) -> Unit) {
        takePhotoCallback = callback
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        cameraLauncher.launch(Intent(this, CameraCaptureActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        resetIdleTimer()
    }

    override fun openWifi() {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetIdleTimer()
    }

    override fun onPause() {
        super.onPause()
        removeCallbacks()
    }

    private fun resetIdleTimer() {
        removeCallbacks()
        handler.postDelayed(idleRunnable, idleTimeout)
    }

    private fun showScreensaver() {
        AppComponent.screensaverHandler.invoke()
    }

    private fun removeCallbacks() {
        handler.removeCallbacks(idleRunnable)
    }
}
