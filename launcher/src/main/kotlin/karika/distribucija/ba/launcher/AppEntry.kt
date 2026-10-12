package karika.distribucija.ba.launcher

import android.content.Context
import karika.distribucija.ba.launcher.provision.DeviceConfig

data class AppEntry(
    /** The device API's name for this app - the `app` QR provisioning extra and the `app` param of
     * GET /version both use it. */
    val key: String,
    val packageName: String,
    val label: String
)

/** Apps the launcher can be provisioned to run. Add more entries here as they're onboarded. */
object KnownApps {
    val SALESREP = AppEntry(key = "salesrep", packageName = "karika.distribucija.ba.salesrep", label = "Karika - komercijalisti")
    val SHOP = AppEntry(key = "shop", packageName = "karika.distribucija.ba.kiosk", label = "Karika")

    val ALL = listOf(SALESREP, SHOP)

    /** The one app this device runs - the one UpdateWorker keeps up to date and LauncherActivity
     * auto-relaunches after a crash. Chosen per device by the `app` QR provisioning extra (see
     * DeviceConfig), salesrep when it's absent, so devices provisioned before it existed keep
     * running what they always have. */
    fun primary(context: Context): AppEntry =
        ALL.firstOrNull { it.key == DeviceConfig.app(context) } ?: SALESREP
}
