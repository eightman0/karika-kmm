package karika.distribucija.ba

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import java.io.ByteArrayOutputStream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Plain payload activity - lock task, Home role and updates are all the launcher's job (it is
 * the Device Owner and installs this app through the admin dashboard), not this app's. */
class MainActivityKiosk : KarikaActivity() {
    private val idleTimeout = 20000L
    private val handler = Handler(Looper.getMainLooper())
    private val idleRunnable = Runnable {
        showScreensaver()
    }
    private var takePhotoCallback: ((String, ByteArray) -> Unit)? = null

    @OptIn(ExperimentalUuidApi::class)
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                takePhotoCallback?.invoke(
                    Uuid.random().toString() + ".png",
                    bitmapToByteArray(bitmap)
                )
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
        fun takePicture() {
            takePhotoCallback = callback
            takePictureLauncher.launch()
        }

        takePicture()
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

fun bitmapToByteArray(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(format, quality, stream)
    return stream.toByteArray()
}