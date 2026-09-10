package karika.distribucija.ba.salesrep.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ActivityCameraCaptureBinding
import java.io.File

/**
 * In-app photo capture via CameraX, instead of delegating to a system Camera app through an
 * implicit MediaStore.ACTION_IMAGE_CAPTURE intent - a kiosk provisioned with
 * PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED=false (see admin-dashboard/app/provisioning.py) may
 * not have one installed to receive it at all.
 *
 * Saves the photo to this app's own cache dir and hands back its absolute path via
 * EXTRA_RESULT_PATH - never leaves this process, so no FileProvider/content Uri is needed. Caller
 * (AttachmentPicker) reads the bytes and deletes the file.
 */
class CameraCaptureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraCaptureBinding
    private var imageCapture: ImageCapture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraCaptureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonClose.setOnClickListener { finish() }
        binding.buttonShutter.setOnClickListener { capture() }

        // AttachmentPicker already requests this before launching us - re-checked here in case it
        // was revoked in the gap between that check and this activity actually starting.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.cameraPreview.surfaceProvider
            }
            val capture = ImageCapture.Builder().build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                imageCapture = capture
            } catch (e: Exception) {
                Toast.makeText(this, R.string.camera_start_failed, Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capture() {
        val capture = imageCapture ?: return
        binding.buttonShutter.isEnabled = false
        val file = File(cacheDir, "capture_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(
            output,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val result = Intent().putExtra(EXTRA_RESULT_PATH, file.absolutePath)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    binding.buttonShutter.isEnabled = true
                    Toast.makeText(
                        this@CameraCaptureActivity, R.string.camera_capture_failed, Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    companion object {
        const val EXTRA_RESULT_PATH = "capture_path"
    }
}
