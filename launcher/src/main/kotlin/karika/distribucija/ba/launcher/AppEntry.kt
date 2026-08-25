package karika.distribucija.ba.launcher

data class AppEntry(
    val packageName: String,
    val label: String
)

/** Apps the launcher shows and can auto-relaunch. Add more entries here as they're onboarded. */
object KnownApps {
    val ALL = listOf(
        AppEntry(packageName = "karika.distribucija.ba.salesrep", label = "Karika - komercijalisti")
    )

    /** The app UpdateWorker keeps up to date and LauncherActivity auto-relaunches after a crash. */
    val PRIMARY = ALL.first()
}
