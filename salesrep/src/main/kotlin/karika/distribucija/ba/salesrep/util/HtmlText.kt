package karika.distribucija.ba.salesrep.util

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.core.text.HtmlCompat

/** Renders [html] markup (bold, italic, links, paragraphs, etc.) instead of showing it as raw
 * tags, mirroring composeApp's HtmlTextWithStyles (ui/common/PlatformSpecific.kt) which parses
 * order comments through a rich-text HTML renderer rather than plain text. */
fun TextView.setHtmlText(html: String) {
    text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
    movementMethod = LinkMovementMethod.getInstance()
}
