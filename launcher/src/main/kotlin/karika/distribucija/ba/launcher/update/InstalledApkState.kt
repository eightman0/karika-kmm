package karika.distribucija.ba.launcher.update

import android.content.Context

/**
 * Tracks the sha256 of the salesrep build this device last installed through the updater, so
 * UpdateWorker can decide whether to install by comparing actual file content instead of trusting
 * version numbers to be entered correctly and to increase monotonically - publishing only needs a
 * new APK, not a carefully bumped version code.
 */
object InstalledApkState {
    private const val PREFS = "installed_apk_state"
    private const val KEY_SHA256 = "last_installed_sha256"

    fun lastInstalledSha256(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SHA256, null)

    fun setLastInstalledSha256(context: Context, sha256: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SHA256, sha256)
            .apply()
    }
}
