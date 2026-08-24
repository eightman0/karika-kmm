package karika.distribucija.ba.launcher

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.launcher.provision.LauncherKiosk

class LauncherActivity : AppCompatActivity() {
    private lateinit var kiosk: LauncherKiosk
    private lateinit var appGrid: RecyclerView
    private lateinit var maintenanceBanner: View

    /** Fires the moment UpdateWorker's begin()/end() writes, so the banner doesn't stay stuck
     * showing "maintenance" if it ended while this Activity was already resumed and on screen. */
    private val maintenanceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refreshMaintenanceState() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kiosk = LauncherKiosk(this)
        setContentView(R.layout.activity_launcher)

        appGrid = findViewById(R.id.app_grid)
        maintenanceBanner = findViewById(R.id.maintenance_banner)
        appGrid.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        appGrid.adapter = AppTileAdapter(KnownApps.ALL, packageManager) { app ->
            launchApp(app.packageName, userInitiated = true)
        }
    }

    override fun onResume() {
        super.onResume()
        kiosk.enter()
        MaintenanceState.addChangeListener(this, maintenanceListener)
        refreshMaintenanceState()
    }

    override fun onPause() {
        MaintenanceState.removeChangeListener(this, maintenanceListener)
        super.onPause()
    }

    /**
     * The launcher is the registered HOME activity, so this only resumes when nothing else is in
     * front of the locked task - i.e. the payload app crashed, finished, or isn't installed yet.
     * That's the natural trigger for auto-relaunch, no polling/foreground-detection needed.
     */
    private fun refreshMaintenanceState() {
        val inMaintenance = MaintenanceState.isActive(this)
        maintenanceBanner.visibility = if (inMaintenance) View.VISIBLE else View.GONE
        appGrid.visibility = if (inMaintenance) View.GONE else View.VISIBLE

        if (!inMaintenance) {
            launchApp(KnownApps.PRIMARY.packageName, userInitiated = false)
        }
    }

    @Suppress("DEPRECATION")
    private fun launchApp(packageName: String, userInitiated: Boolean) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        if (!userInitiated && !RelaunchGuard.shouldAutoLaunch(this, packageName)) return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        // Belt-and-suspenders on top of the theme's windowAnimationStyle overrides: this covers
        // the leg we explicitly trigger (launching salesrep), in case any animation category the
        // theme doesn't catch still applies to an explicit startActivity() call.
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val SPAN_COUNT = 4
    }
}
