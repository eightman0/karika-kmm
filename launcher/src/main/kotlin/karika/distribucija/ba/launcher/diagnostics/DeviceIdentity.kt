package karika.distribucija.ba.launcher.diagnostics

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

object DeviceIdentity {
    /** Stable per-device identifier (survives app reinstall, resets on factory reset) - the
     * deviceId POST /enroll registers this device under, and the FCM per-device topic. */
    @SuppressLint("HardwareIds")
    fun id(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}
