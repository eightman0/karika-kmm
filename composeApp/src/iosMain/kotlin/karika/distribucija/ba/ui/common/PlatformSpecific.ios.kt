package karika.distribucija.ba.ui.common

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.PlatformImeOptions
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openPdf(url: String) {
    val nsUrl = NSURL.URLWithString(url.replace("\\", "")) ?: return
    val app = UIApplication.sharedApplication
    app.openURL(url = nsUrl, completionHandler = {}, options = mapOf<Any?, String>())
}

actual fun getEnvPrefix(): String {
    val currentEnv =
        NSBundle.mainBundle.objectForInfoDictionaryKey("APP_ENV") as? String ?: "prod"
    return when (currentEnv) {
        "prod" -> ""
        "demo" -> "demo."
        "stage" -> "stage."
        else -> "test."
    }
}

actual fun isKiosk() = false

actual fun appVersion(): Int {
    return (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        ?: "0.0")
        .replace(".", "")
        .toIntOrNull() ?: 0
}

actual fun openPhoneCall(phoneNumber: String, error: (String) -> Unit) {
    UIApplication.sharedApplication.openURL(
        url = NSURL(string = "tel://$phoneNumber"),
        options = mapOf<Any?, String>(),
        completionHandler = {
            if (!it) {
                error.invoke(it.toString())
            }
        }
    )
}

actual fun getEnvJwt(): String {
    val currentEnv =
        NSBundle.mainBundle.objectForInfoDictionaryKey("APP_ENV") as? String ?: "prod"
    return when (currentEnv) {
        "prod" -> "lbzgyy1qylr7unu707eblcphftb2fzha"
        "demo", "stage" -> "hgy5au3paxuijsiv52nyt9w47fcxprbz"
        else -> "09kqzjtmz5cf1klm9hjxw9yt3uaa63hk"
    }
}

actual fun appVersionName(): String {
    return "v${NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "0.0"}(${
        NSBundle.mainBundle.infoDictionary?.get(
            "CFBundleVersion"
        ) as? String ?: "1"
    })"
}

actual fun openEmail(emailAddress: String, error: (String) -> Unit) {
    UIApplication.sharedApplication.openURL(
        url = NSURL(string = "mailto:$emailAddress"),
        options = mapOf<Any?, String>(),
        completionHandler = {
            if (!it) {
                error.invoke(it.toString())
            }
        }
    )
}

actual fun isAndroid(): Boolean {
    return false
}

actual fun appUrl(): String {
    return "https://apps.apple.com/app/id/6692625868"
}

actual fun userAgent(): String {
    return "os:iOS;version:${appVersionName()}"
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun textFieldImeOptions(
    onDone: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    useAccessoryView: Boolean
): PlatformImeOptions? {
    // Only use inputAccessoryView when explicitly requested
    // because it interferes with IME visibility detection
    if (!useAccessoryView) {
        return null
    }

    return PlatformImeOptions {
        // Don't set keyboardType here - it interferes with IME detection
        // Let the TextField control its own keyboard type
        inputAccessoryView(
            createInputAccessoryToolbar(
                onPrevious = onPrevious,
                onNext = onNext,
                onDone = onDone
            )
        )
    }
}
