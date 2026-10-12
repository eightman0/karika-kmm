package karika.distribucija.ba

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * In-app photo capture via CameraX - the kiosk's replacement for an implicit IMAGE_CAPTURE
 * intent, which crashed with ActivityNotFoundException: the launcher provisions tablets with
 * PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED=false, so there is no Camera app to receive it.
 * Same approach as salesrep's CameraCaptureActivity.
 *
 * The photo is written upright (rotated from the sensor's orientation in pixels, not via an EXIF
 * tag the server may ignore) as a JPEG in this app's cache dir, and its path is handed back via
 * EXTRA_RESULT_PATH. MainActivityKiosk reads the bytes and deletes the file. CAMERA permission is
 * requested by MainActivityKiosk before starting this.
 */
class CameraCaptureActivity : ComponentActivity() {

    private var imageCapture: ImageCapture? = null
    private val capturing = mutableStateOf(false)
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Kamera nije dozvoljena", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PreviewView(context).also { startCamera(it) }
                    }
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(20.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { finish() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 22.sp)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 40.dp)
                        .size(80.dp)
                        .clip(CircleShape)
                        // Dark backdrop so the white shutter still shows against a bright preview.
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(4.dp, Color.White, CircleShape)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(if (capturing.value) Color.Gray else Color.White)
                        .clickable(enabled = !capturing.value) { capture() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera(previewView: PreviewView) {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                val provider = providerFuture.get()
                // Kiosk tablets may be mounted with only the front camera usable - prefer the back.
                val selector = when {
                    provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                    else -> CameraSelector.DEFAULT_FRONT_CAMERA
                }
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                // Capped around 1920x1440: a full-sensor shot is several MB to upload as an
                // attachment and gains nothing on a message or bill photo.
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(1920, 1440),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
                                )
                            )
                            .build()
                    )
                    .build()
                provider.unbindAll()
                provider.bindToLifecycle(this, selector, preview, capture)
                imageCapture = capture
            } catch (e: Exception) {
                Toast.makeText(this, "Kamera se ne može pokrenuti", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun capture() {
        val capture = imageCapture ?: return
        capturing.value = true
        capture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val file = try {
                    image.use { saveUpright(it) }
                } catch (e: Exception) {
                    null
                }
                runOnUiThread {
                    if (file == null) {
                        onCaptureFailed()
                    } else {
                        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_RESULT_PATH, file.absolutePath))
                        finish()
                    }
                }
            }

            override fun onError(exception: ImageCaptureException) {
                runOnUiThread { onCaptureFailed() }
            }
        })
    }

    private fun onCaptureFailed() {
        capturing.value = false
        Toast.makeText(this, "Slikanje nije uspjelo, pokušajte ponovo", Toast.LENGTH_SHORT).show()
    }

    private fun saveUpright(image: ImageProxy): File {
        val bitmap = image.toBitmap()
        val rotation = image.imageInfo.rotationDegrees
        val upright = if (rotation == 0) {
            bitmap
        } else {
            Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height,
                Matrix().apply { postRotate(rotation.toFloat()) }, true
            ).also { bitmap.recycle() }
        }
        val file = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { upright.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
        upright.recycle()
        return file
    }

    companion object {
        const val EXTRA_RESULT_PATH = "capture_path"
        private const val JPEG_QUALITY = 85
    }
}
