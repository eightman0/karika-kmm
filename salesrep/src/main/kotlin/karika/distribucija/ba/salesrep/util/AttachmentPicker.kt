package karika.distribucija.ba.salesrep.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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

    /** Restricted to PDF, matching composeApp's `pickFile(mediaTypes = arrayOf("application/pdf"))`
     * default - the only way it's ever called from a message screen's attach sheet. */
    fun pickFile() = filePicker.launch(arrayOf("application/pdf"))

    fun pickPhoto() = photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    /** No runtime permission check/request here - a normal request crashed Permission Controller
     * on a real device (it can't present its dialog over a lock-task-pinned kiosk activity), which
     * then froze the launcher's input, showing up there as an ANR. CAMERA is silently granted by
     * the launcher's Device Owner policies instead (see LauncherKiosk.setKioskPolicies()) - this
     * app is never meant to ask for it. CameraCaptureActivity still re-checks on its own before
     * actually opening the camera, as a safety net in case that grant hasn't landed yet. */
    fun takePhoto() = launchCamera()

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
