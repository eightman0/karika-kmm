package karika.distribucija.ba.salesrep.util

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

/**
 * Picks a file or photo via the permission-free system pickers - Storage Access Framework's
 * "open document" for files, the system Photo Picker for images. Mirrors composeApp's
 * KarikaHandler.pickFile()/pickPhoto() (Android impl in KarikaActivity.kt), which use the same
 * two pickers and, per composeApp's AndroidManifest.xml (only INTERNET declared), need no
 * runtime permissions either.
 *
 * Must be constructed as a Fragment property (assigned before the fragment reaches STARTED,
 * e.g. as a class-body property initializer) - `registerForActivityResult` requires that timing.
 */
class AttachmentPicker(
    fragment: Fragment,
    private val onPicked: (filename: String, bytes: ByteArray) -> Unit
) {
    private val filePicker = fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { readAndDeliver(fragment, it) }
    }

    private val photoPicker = fragment.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { readAndDeliver(fragment, it) }
    }

    /** Restricted to PDF, matching composeApp's `pickFile(mediaTypes = arrayOf("application/pdf"))`
     * default - the only way it's ever called from a message screen's attach sheet. */
    fun pickFile() = filePicker.launch(arrayOf("application/pdf"))

    fun pickPhoto() = photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    private fun readAndDeliver(fragment: Fragment, uri: Uri) {
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
