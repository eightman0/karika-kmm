package karika.distribucija.ba.salesrep.ui.messages

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.util.isImageAttachmentFile

/** Initials of a name for the message avatars, at most two letters - mirrors composeApp's
 * KarikaUi.kt `String?.initials()` ("Mesna industrija Vimes" -> "MI"). */
internal fun String?.messageInitials(): String {
    val words = orEmpty()
        .replace(Regex("[„“\"'.,]"), " ")
        .split(" ")
        .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.first().take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

/** The role label of an internal (staff) counterpart - composeApp's salesStaffRole(). */
internal fun Context.staffRoleLabel(counterpartType: String?): String = when (counterpartType) {
    "vendor_owner" -> getString(R.string.k_msg_role_vendor_owner)
    "vendor_employee" -> getString(R.string.k_msg_role_vendor_employee)
    else -> counterpartType.orEmpty()
}

/** Places a chat bubble like SalesChatBubble: own messages on the right, the other side's on the
 * left, 48dp kept free on the opposite side and the bubble's content aligned to the same edge. */
internal fun LinearLayout.alignBubble(mine: Boolean) {
    val side = (48 * resources.displayMetrics.density).toInt()
    val params = layoutParams as FrameLayout.LayoutParams
    params.gravity = if (mine) Gravity.END else Gravity.START
    params.marginStart = if (mine) side else 0
    params.marginEnd = if (mine) 0 else side
    layoutParams = params
    gravity = if (mine) Gravity.END else Gravity.START
}

/** Customer/admin message bodies are HTML in SalesChatBubble (html = true); plain bodies stay
 * as typed so their line breaks survive. */
internal fun String.asMessageBody(): CharSequence =
    if (Regex("<[a-zA-Z/][^>]*>").containsMatchIn(this)) {
        HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_COMPACT).trimEnd()
    } else {
        this
    }

/** The pending attachment above the message field (SalesPendingAttachment): an image shows as a
 * 64dp rounded thumbnail, anything else as a soft blue chip with the pdf/attachment icon and the
 * name cut to 32 characters; the remove circle sits on the top end corner of either. */
internal fun renderPendingAttachment(
    container: View,
    preview: ImageView,
    chip: View,
    icon: ImageView,
    name: TextView,
    attachment: Pair<String, ByteArray>?
) {
    container.visibility = if (attachment != null) View.VISIBLE else View.GONE
    if (attachment == null) {
        preview.setImageDrawable(null)
        return
    }
    val (fileName, bytes) = attachment
    val thumbnail = if (isImageAttachmentFile(fileName)) bytes.decodeThumbnail(preview.resources.displayMetrics.density) else null
    preview.visibility = if (thumbnail != null) View.VISIBLE else View.GONE
    chip.visibility = if (thumbnail != null) View.GONE else View.VISIBLE
    if (thumbnail != null) {
        preview.clipToOutline = true
        preview.setImageBitmap(thumbnail)
    } else {
        icon.setImageResource(
            if (fileName.lowercase().endsWith(".pdf")) R.drawable.ic_pdf else R.drawable.ic_attachment
        )
        name.text = fileName.take(32)
    }
}

/** Decodes a picked photo down-sampled to about the 64dp thumbnail, so big camera shots stay cheap. */
private fun ByteArray.decodeThumbnail(density: Float): Bitmap? {
    val target = (64 * density).toInt().coerceAtLeast(1)
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(this, 0, size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) sample *= 2
    return BitmapFactory.decodeByteArray(this, 0, size, BitmapFactory.Options().apply { inSampleSize = sample })
}
