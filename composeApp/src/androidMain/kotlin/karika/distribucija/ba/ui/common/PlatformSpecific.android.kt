package karika.distribucija.ba.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import karika.distribucija.ba.BuildConfig
import org.koin.mp.KoinPlatform

@Composable
actual fun HtmlTextWithStyles1(
    modifier: Modifier,
    html: String,
    background: Color,
    textColor: Color,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->

            AppCompatTextView(ctx).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                text = html.parseAsHtml()
                setTextColor(textColor.toArgb())
            }
        }
    )
}

actual fun openPdf(url: String) {
    val context: Context = KoinPlatform.getKoin().get()

    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                url.toUri()
            ).apply { addFlags(FLAG_ACTIVITY_NEW_TASK) }
        )
    } catch (e: ActivityNotFoundException) {

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
        "demo" -> "hgy5au3paxuijsiv52nyt9w47fcxprbz"
        else -> "09kqzjtmz5cf1klm9hjxw9yt3uaa63hk"
    }
}

actual fun appVersionName(): String {
    return "v${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
}

actual fun openEmail(emailAddress: String, error: (String) -> Unit) {
    val context: Context = KoinPlatform.getKoin().get()
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:$emailAddress".toUri()
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