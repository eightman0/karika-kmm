package karika.distribucija.ba.salesrep.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/** Mirrors composeApp's PlatformSpecific.android.kt `openPdf(url)` - opens the URL externally via
 * ACTION_VIEW and silently swallows a missing-handler failure, exactly like the Compose source. */
fun Context.openPdfExternally(url: String) {
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW, url.toUri()).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
    } catch (e: ActivityNotFoundException) {
    }
}
