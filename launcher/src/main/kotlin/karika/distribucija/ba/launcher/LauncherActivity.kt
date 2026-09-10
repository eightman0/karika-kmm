package karika.distribucija.ba.launcher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.launcher.provision.LauncherKiosk
import karika.distribucija.ba.logging.AppLogger

class LauncherActivity : AppCompatActivity() {
    private lateinit var kiosk: LauncherKiosk
    private lateinit var appGrid: RecyclerView
    private lateinit var maintenanceBanner: View

    /** Fires the moment UpdateWorker's begin()/end() writes, so the banner doesn't stay stuck
     * showing "maintenance" if it ended while this Activity was already resumed and on screen. */
    private val maintenanceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refreshMaintenanceState() }

    /** One-time, for THIS app's own package only - not salesrep's, see git history for why that
     * one was dropped entirely. Fires as early as the very first onResume() after provisioning,
     * with no dependency on salesrep being installed yet (unlike the old salesrep version), and
     * gates UpdateWorker's first-ever salesrep install (see LauncherBatteryOptimization) so that
     * install does not start while the technician is still looking at this dialog. The result
     * callback fires once they return from Settings (Allow or Deny, does not matter which) - that
     * is what marks it resolved and ends maintenance, not a timer. */
    private val batteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            LauncherBatteryOptimization.markResolved(this)
            MaintenanceState.end(this)
            refreshMaintenanceState()
        }

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
        AppLogger.i(TAG, "onResume")
        // Must run before kiosk.enter(): when it does begin maintenance, that has to happen
        // before kiosk.enter() re-engages lock task below, in this same pass, or the Settings
        // dialog can't draw over the pinned kiosk activity. Retried on every onResume() (not just
        // once in onCreate()) so a resume that lands before it's resolved does not skip it.
        maybeRequestOwnBatteryExemption()
        kiosk.enter()
        MaintenanceState.addChangeListener(this, maintenanceListener)
        RemoteMaintenanceState.addChangeListener(this, maintenanceListener)
        refreshMaintenanceState()
    }

    override fun onPause() {
        MaintenanceState.removeChangeListener(this, maintenanceListener)
        RemoteMaintenanceState.removeChangeListener(this, maintenanceListener)
        super.onPause()
    }

    /**
     * The launcher is the registered HOME activity, so this only resumes when nothing else is in
     * front of the locked task - i.e. the payload app crashed, finished, or isn't installed yet.
     * That's the natural trigger for auto-relaunch, no polling/foreground-detection needed.
     */
    private fun refreshMaintenanceState() {
        val inMaintenance = MaintenanceState.isActive(this) || RemoteMaintenanceState.isActive(this)
        maintenanceBanner.visibility = if (inMaintenance) View.VISIBLE else View.GONE
        appGrid.visibility = if (inMaintenance) View.GONE else View.VISIBLE

        if (!inMaintenance) {
            launchApp(KnownApps.PRIMARY.packageName, userInitiated = false)
        }
    }

    /** See batteryOptimizationLauncher's own doc comment. Guarded by LauncherBatteryOptimization's
     * resolved flag (one-shot) and by MaintenanceState.isActive() - the latter so a second
     * onResume() landing before the technician has responded (screen touch, etc.) does not launch
     * a second copy of the same system dialog on top of the first. */
    private fun maybeRequestOwnBatteryExemption() {
        if (LauncherBatteryOptimization.isResolved(this)) return
        if (MaintenanceState.isActive(this)) return
        val powerManager = getSystemService(PowerManager::class.java)
        if (powerManager?.isIgnoringBatteryOptimizations(packageName) == true) {
            LauncherBatteryOptimization.markResolved(this)
            return
        }

        MaintenanceState.begin(this)
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        runCatching { batteryOptimizationLauncher.launch(intent) }
            .onFailure {
                LauncherBatteryOptimization.markResolved(this)
                MaintenanceState.end(this)
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
        private const val TAG = "LauncherActivity"
        private const val SPAN_COUNT = 4

        /** Used remotely (maintenance-on) to pull the launcher back over whatever's currently on
         * top, without waiting for it to resume naturally (e.g. salesrep crashing/finishing). */
        fun bringToFront(context: Context) {
            AppLogger.i(TAG, "bringToFront")
            val intent = Intent(context, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
