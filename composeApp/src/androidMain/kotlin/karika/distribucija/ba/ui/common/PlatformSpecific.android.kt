package karika.distribucija.ba.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Patterns
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.core.net.toUri
import karika.distribucija.ba.BuildConfig
import org.koin.mp.KoinPlatform

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
        "stage" -> "stage."
        else -> "test."
    }
}

actual fun isKiosk() = BuildConfig.FLAVOR.startsWith("kiosk")
actual fun appVersion(): Int {
    return BuildConfig.VERSION_NAME.replace(".", "").toIntOrNull() ?: 0
}

actual fun openPhoneCall(phoneNumber: String, error: (String) -> Unit) {
    val context: Context = KoinPlatform.getKoin().get()
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:$phoneNumber".toUri()
        flags = FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(intent)
    } catch (ignored: Exception) {
        error.invoke(ignored.message ?: "")
    }
}

actual fun getEnvJwt(): String {
    return when (BuildConfig.FLAVOR) {
        "prod", "kiosk" -> "lbzgyy1qylr7unu707eblcphftb2fzha"
        "demo", "stage" -> "hgy5au3paxuijsiv52nyt9w47fcxprbz"
        else -> "09kqzjtmz5cf1klm9hjxw9yt3uaa63hk"
    }
}

actual fun appVersionName(): String {
    return "v${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
}

actual fun openEmail(emailAddress: String, error: (String) -> Unit) {
    if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
        error.invoke("Email is not valid!")
        return
    }

    val context: Context = KoinPlatform.getKoin().get()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:$emailAddress".toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        error.invoke("No email app installed!")
    } catch (e: Exception) {
        error.invoke(e.message ?: "Failed to open email app")
    }
}

actual fun isAndroid(): Boolean {
    return !isKiosk()
}

actual fun appUrl(): String {
    return "https://${getEnvPrefix()}karika.ba/internal/builds/app"
}

actual fun userAgent(): String {
    return "os:Android;version:${appVersionName()}"
}

actual fun textFieldImeOptions(
    onDone: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    useAccessoryView: Boolean
): PlatformImeOptions? = null