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

class LauncherActivity : AppCompatActivity() {
    private lateinit var kiosk: LauncherKiosk
    private lateinit var appGrid: RecyclerView
    private lateinit var maintenanceBanner: View

    /** Fires the moment UpdateWorker's begin()/end() writes, so the banner doesn't stay stuck
     * showing "maintenance" if it ended while this Activity was already resumed and on screen. */
    private val maintenanceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refreshMaintenanceState() }

    /** One-time (per install), retried on every resume until it succeeds: asks for salesrep's
     * battery-optimization
     * exemption (see LocationSampleWorker/BatteryOptimizationPrompt's old removed comment for why
     * salesrep needs it and this app does not) as a normal Settings dialog, not a silent Device
     * Owner grant - that specific DPM call is what crashes Permission Controller on a real device
     * (see LauncherKiosk's own comment).
     *
     * Wrapped in maintenance mode instead of a guessed grace period: entering it hides the kiosk
     * grid and skips auto-launching salesrep (see refreshMaintenanceState()), and LauncherKiosk
     * skips re-engaging lock task for as long as it stays active, so the dialog gets the screen
     * for exactly as long as it is actually open - no race with a relaunch landing mid-interaction
     * the way a fixed timer had. The result callback fires once the technician returns from
     * Settings (Allow or Deny, does not matter which), and that is what ends maintenance - not a
     * timer.
     */
    private val batteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
        // Must run before kiosk.enter(): on a fresh provisioning, salesrep is not installed yet
        // (UpdateWorker installs it later, asynchronously) - see maybeRequestSalesrepBatteryExemption's
        // own comment for why this call is retried on every onResume() instead of once in onCreate().
        // When it does begin maintenance, it needs to do so before kiosk.enter() runs below, in this
        // same pass, or lock task gets pinned first and the Settings dialog can't draw over it.
        maybeRequestSalesrepBatteryExemption()
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

    /** See batteryOptimizationLauncher's own doc comment for the full reasoning. Guarded by a
     * persisted flag so this only ever fires once per install, and by isIgnoringBatteryOptimizations
     * so it does nothing at all once salesrep already has the exemption (e.g. granted manually via
     * `adb shell dumpsys deviceidle whitelist`) - no dialog shown, no maintenance entered.
     *
     * Also guarded by salesrep actually being installed: on a fresh provisioning this runs long
     * before UpdateWorker has downloaded and installed it, and the Settings dialog silently does
     * nothing for a package that doesn't exist yet - it must NOT mark the "asked" flag in that
     * case, or this would burn its one shot before salesrep even exists and never ask again once it
     * does. Not installed simply means try again next onResume(), no state changed here. */
    private fun maybeRequestSalesrepBatteryExemption() {
        val prefs = getSharedPreferences(BATTERY_PROMPT_PREFS, MODE_PRIVATE)
        if (prefs.getBoolean(BATTERY_PROMPT_KEY_ASKED, false)) return
        val targetPackage = KnownApps.PRIMARY.packageName
        if (!isPackageInstalled(targetPackage)) return
        val powerManager = getSystemService(PowerManager::class.java)
        if (powerManager?.isIgnoringBatteryOptimizations(targetPackage) == true) return
        prefs.edit().putBoolean(BATTERY_PROMPT_KEY_ASKED, true).apply()

        MaintenanceState.begin(this)
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$targetPackage")
        }
        runCatching { batteryOptimizationLauncher.launch(intent) }
            .onFailure { MaintenanceState.end(this) }
    }

    private fun isPackageInstalled(packageName: String): Boolean =
        runCatching { packageManager.getPackageInfo(packageName, 0) }.isSuccess

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
        private const val BATTERY_PROMPT_PREFS = "battery_optimization_prompt"
        private const val BATTERY_PROMPT_KEY_ASKED = "asked"

        /** Used remotely (maintenance-on) to pull the launcher back over whatever's currently on
         * top, without waiting for it to resume naturally (e.g. salesrep crashing/finishing). */
        fun bringToFront(context: Context) {
            val intent = Intent(context, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
