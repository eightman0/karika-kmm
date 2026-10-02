package karika.distribucija.ba.launcher.update

import android.content.Context
import karika.distribucija.ba.launcher.KnownApps

/**
 * Tracks, per package, the sha256 of the payload build this device last installed through the updater, so
 * UpdateWorker can decide whether to install by comparing actual file content instead of trusting
 * version numbers to be entered correctly and to increase monotonically - publishing only needs a
 * new APK, not a carefully bumped version code.
 */
object InstalledApkState {
    private const val PREFS = "installed_apk_state"
    private const val KEY_SHA256 = "last_installed_sha256"

    // Salesrep keeps the original un-suffixed key, so devices that already installed it before
    // this was per-package don't see an unknown sha and reinstall the same build.
    private fun key(packageName: String) =
        if (packageName == KnownApps.SALESREP.packageName) KEY_SHA256 else "${KEY_SHA256}_$packageName"

    fun lastInstalledSha256(context: Context, packageName: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key(packageName), null)

    fun setLastInstalledSha256(context: Context, packageName: String, sha256: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(key(packageName), sha256)
            .apply()
    }
}
