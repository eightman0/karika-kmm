package karika.distribucija.ba.ui.common

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.PlatformImeOptions
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSBundle
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.resume

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

@OptIn(ExperimentalForeignApi::class)
private class LocationDelegate(
    private val onAuthChange: (CLAuthorizationStatus) -> Unit,
    private val onLocation: (CLLocation?) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        onAuthChange(manager.authorizationStatus)
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        onLocation(didUpdateLocations.lastOrNull() as? CLLocation)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onLocation(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private val locationManager: CLLocationManager by lazy { CLLocationManager() }

@OptIn(ExperimentalForeignApi::class)
private fun isAuthorized(status: CLAuthorizationStatus): Boolean =
    status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse

@OptIn(ExperimentalForeignApi::class)
actual suspend fun requestLocationPermission(): Boolean {
    val status = locationManager.authorizationStatus
    if (isAuthorized(status)) return true
    if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) return false

    return suspendCancellableCoroutine { continuation ->
        val delegate = LocationDelegate(
            onAuthChange = { newStatus ->
                if (newStatus != kCLAuthorizationStatusNotDetermined && continuation.isActive) {
                    continuation.resume(isAuthorized(newStatus))
                }
            },
            onLocation = {}
        )
        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun currentDeviceLocation(): DeviceLocationFix? {
    if (!isAuthorized(locationManager.authorizationStatus)) return null

    return suspendCancellableCoroutine { continuation ->
        val delegate = LocationDelegate(
            onAuthChange = {},
            onLocation = { location ->
                if (continuation.isActive) {
                    continuation.resume(location?.toFix())
                }
            }
        )
        locationManager.delegate = delegate
        locationManager.requestLocation()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toFix(): DeviceLocationFix {
    val (lat, lon) = coordinate.useContents { latitude to longitude }
    return DeviceLocationFix(
        latitude = lat,
        longitude = lon,
        accuracy = horizontalAccuracy.takeIf { it >= 0 },
        altitude = altitude,
        speed = speed.takeIf { it >= 0 },
        heading = course.takeIf { it >= 0 },
    )
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
