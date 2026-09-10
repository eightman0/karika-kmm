package karika.distribucija.ba.salesrep.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import karika.distribucija.ba.salesrep.ui.camera.CameraCaptureActivity
import java.io.File

/**
 * Picks a file, an existing photo, or a freshly-taken one. File/photo go through the
 * permission-free system pickers - Storage Access Framework's "open document" for files, the
 * system Photo Picker for images - mirroring composeApp's KarikaHandler.pickFile()/pickPhoto()
 * (Android impl in KarikaActivity.kt). Taking a photo goes through this app's own CameraX screen
 * (see CameraCaptureActivity) rather than an external Camera app intent, since a kiosk provisioned
 * with PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED=false may not have one installed to receive it.
 *
 * Must be constructed as a Fragment property (assigned before the fragment reaches STARTED,
 * e.g. as a class-body property initializer) - `registerForActivityResult` requires that timing.
 */
class AttachmentPicker(
    private val fragment: Fragment,
    private val onPicked: (filename: String, bytes: ByteArray) -> Unit
) {
    private val filePicker = fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { readAndDeliver(it) }
    }

    private val photoPicker = fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { readAndDeliver(it) }
    }

    private val cameraLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val path = result.data?.getStringExtra(CameraCaptureActivity.EXTRA_RESULT_PATH) ?: return@registerForActivityResult
            val file = File(path)
            if (!file.exists()) return@registerForActivityResult
            val bytes = file.readBytes()
            file.delete()
            onPicked(file.name, bytes)
        }

    private val cameraPermissionLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
        }

    /** Restricted to PDF, matching composeApp's `pickFile(mediaTypes = arrayOf("application/pdf"))`
     * default - the only way it's ever called from a message screen's attach sheet. */
    fun pickFile() = filePicker.launch(arrayOf("application/pdf"))

    fun pickPhoto() = photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    /** A plain runtime permission request - this app is always the current lock-task-allowed
     * foreground app when it asks, so unlike a cross-app system dialog (ADB authorization,
     * battery-optimization) this one has nothing to draw over and needs no special handling. Was
     * briefly replaced with a silent Device-Owner grant instead, after mistakenly attributing a
     * real-device Permission Controller crash to this call - the actual cause turned out to be a
     * broken OS notifier specific to DevicePolicyManager.setPermissionGrantState() (see
     * LauncherKiosk's own comment), unrelated to this normal request. */
    fun takePhoto() {
        val context = fragment.context ?: return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val context = fragment.context ?: return
        cameraLauncher.launch(Intent(context, CameraCaptureActivity::class.java))
    }

    private fun readAndDeliver(uri: Uri) {
        val context = fragment.context ?: return
        var name = "file"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                cursor.getString(index)?.let { name = it }
            }
        }
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
        onPicked(name, bytes)
    }
}

/** Mirrors the 4x-duplicated `String.isImageFile()` private extension in composeApp's
 * SalesCustomer/AdminConversation/NewMessageView.kt - picks the pending-attachment chip's icon
 * (ic_photo vs ic_attachment). */
fun isImageAttachmentFile(filename: String) = filename.lowercase().let {
    it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
        it.endsWith(".gif") || it.endsWith(".webp")
}
