package karika.distribucija.ba.salesrep.util

import android.content.Context
import karika.distribucija.ba.salesrep.ui.pdf.PdfViewerActivity

/** Opens a PDF (or any document URL) on an in-app screen. composeApp's `openPdf(url)` hands it to
 * the browser, but the salesrep app runs kiosk-locked and must not leave the app. */
fun Context.openPdfExternally(url: String, title: String? = null) {
    PdfViewerActivity.open(this, url, title)
}
