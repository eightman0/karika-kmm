package karika.distribucija.ba.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
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
        "prod", "kiosk" -> ""
        "demo" -> "demo."
        else -> "test."
    }
}

actual fun isKiosk() = BuildConfig.FLAVOR.startsWith("kiosk")
actual fun appVersion(): String {
    return ""
}

actual fun openPhoneCall(phoneNumber: String, error: (String) -> Unit) {
    val context: Context = KoinPlatform.getKoin().get()
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
        flags = FLAG_ACTIVITY_NEW_TASK
    }
    try {
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    } catch (ignored: Exception) {
        error.invoke(ignored.message ?: "")
    }
}

actual fun getEnvJwt(): String {
    return when (BuildConfig.FLAVOR) {
        "prod", "kiosk" -> "lbzgyy1qylr7unu707eblcphftb2fzha"
        "demo" -> "demo."
        else -> "09kqzjtmz5cf1klm9hjxw9yt3uaa63hk"
    }
}

actual fun appVersionName(): String {
   return "v${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
}