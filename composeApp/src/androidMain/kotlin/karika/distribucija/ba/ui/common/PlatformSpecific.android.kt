package karika.distribucija.ba.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Html
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.parseAsHtml
import karika.distribucija.ba.BuildConfig
import org.koin.mp.KoinPlatform

@Composable
actual fun HtmlTextWithStyles(
    modifier: Modifier,
    html: String,
    background: Color,
    textColor: Color,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val unescaped = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

            AppCompatTextView(ctx).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                text = unescaped.parseAsHtml()
                setTextColor(textColor.toArgb())
            }
        }
    )
}

actual fun openPdf(url: String) {
    val context: Context = KoinPlatform.getKoin().get()

    val uri = Uri.parse(url)
    val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        context.startActivity(pdfIntent)
    } catch (e: ActivityNotFoundException) {
        try {
            val browser = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browser)
        } catch (_: Exception) {
            val gview = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://docs.google.com/gview?embedded=true&url=$url")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(gview)
        }
    }
}

actual fun getEnvPrefix(): String {
    val flavour = BuildConfig.FLAVOR
    return when (flavour) {
        "prod" -> ""
        "demo" -> "demo."
        else -> "test."
    }
}