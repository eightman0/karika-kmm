package karika.distribucija.ba.launcher.diagnostics

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings

object DeviceIdentity {
    /** Stable per-device identifier (survives app reinstall, resets on factory reset) - used to
     * key the Firestore document and Storage path for this device's diagnostics. */
    @SuppressLint("HardwareIds")
    fun id(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
}
