package karika.distribucija.ba.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.launcher.provision.DeviceConfig
import karika.distribucija.ba.launcher.provision.LauncherKiosk
import karika.distribucija.ba.logging.AppLogger

class LauncherActivity : AppCompatActivity() {
    private lateinit var kiosk: LauncherKiosk
    private lateinit var appGrid: RecyclerView
    private lateinit var statusPanel: View
    private lateinit var statusTitle: TextView
    private lateinit var statusMessage: TextView

    /** Fires the moment UpdateWorker's begin()/end() (or an enroll result) writes, so the panel
     * doesn't stay stuck on a state that ended while this Activity was already on screen. Goes
     * through kiosk.enter() too, not just the panel: lock task is switched off while maintenance
     * is active (see LauncherKiosk.setKioskPolicies), and a payload install replacing the app in
     * front resumes this Activity mid-maintenance - without re-entering here when maintenance
     * ends, the freshly installed app would be launched with lock task still off. */
    private val maintenanceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> reapplyKioskState() }

    /** The payload landing (first install or update) while the launcher is in front: start it
     * right away instead of waiting for some later resume. */
    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.data?.schemeSpecificPart != KnownApps.primary(context).packageName) return
            AppLogger.i(TAG, "Payload installed: ${intent.action}")
            reapplyKioskState()
        }
    }

    /** MaintenanceState's auto-expiry writes nothing, so the listener above never sees it. */
    private val maintenanceExpiryCheck = Runnable { reapplyKioskState() }

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

    /** Hidden service gesture, for when the payload isn't up to offer it (see ServiceUnlockActivity). */
    private val secretTaps by lazy {
        SecretCornerTaps(cornerPx = 96 * resources.displayMetrics.density) { ServiceUnlockActivity.open(this) }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        secretTaps.onTouch(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        kiosk = LauncherKiosk(this)
        setContentView(R.layout.activity_launcher)

        appGrid = findViewById(R.id.app_grid)
        statusPanel = findViewById(R.id.status_panel)
        statusTitle = findViewById(R.id.status_title)
        statusMessage = findViewById(R.id.status_message)
        appGrid.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        appGrid.adapter = AppTileAdapter(listOf(KnownApps.primary(this)), packageManager) { app ->
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
        DeviceConfig.addChangeListener(this, maintenanceListener)
        ContextCompat.registerReceiver(
            this,
            packageReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        refreshMaintenanceState()
    }

    /** singleTop: bringToFront() on an already-resumed launcher (e.g. still showing a status panel
     * right after provisioning, when UpdateWorker finishes the first payload install) lands here
     * instead of onResume() - without this the payload was only started after a reboot. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        AppLogger.i(TAG, "onNewIntent")
        reapplyKioskState()
    }

    override fun onPause() {
        runCatching { unregisterReceiver(packageReceiver) }
        MaintenanceState.removeChangeListener(this, maintenanceListener)
        RemoteMaintenanceState.removeChangeListener(this, maintenanceListener)
        DeviceConfig.removeChangeListener(this, maintenanceListener)
        appGrid.removeCallbacks(maintenanceExpiryCheck)
        super.onPause()
    }

    /**
     * The launcher is the registered HOME activity, so this only resumes when nothing else is in
     * front of the locked task - i.e. the payload app crashed, finished, or isn't installed yet.
     * That's the natural trigger for auto-relaunch, no polling/foreground-detection needed.
     */
    /** Same order as onResume(): lock task first, then whatever the panel/auto-launch decides. */
    private fun reapplyKioskState() {
        kiosk.enter()
        refreshMaintenanceState()
    }

    private fun refreshMaintenanceState() {
        appGrid.removeCallbacks(maintenanceExpiryCheck)
        val expiresInMs = MaintenanceState.remainingMs(this)
        if (expiresInMs > 0) appGrid.postDelayed(maintenanceExpiryCheck, expiresInMs + 1_000)

        val status = currentStatus()
        statusPanel.visibility = if (status != null) View.VISIBLE else View.GONE
        appGrid.visibility = if (status != null) View.GONE else View.VISIBLE
        if (status != null) {
            statusTitle.setText(status.first)
            statusMessage.setText(status.second)
        } else {
            launchApp(KnownApps.primary(this).packageName, userInitiated = false)
        }
    }

    /** Title/message for whatever keeps the payload app from being launched, null when nothing
     * does. Re-provisioning wins over maintenance: no update is coming without a device token. */
    private fun currentStatus(): Pair<Int, Int>? = when {
        DeviceConfig.reprovisionReason(this) == DeviceConfig.ReprovisionReason.QR_INVALID ->
            R.string.qr_invalid_title to R.string.qr_invalid_message
        DeviceConfig.reprovisionReason(this) == DeviceConfig.ReprovisionReason.DEVICE_REMOVED ->
            R.string.device_removed_title to R.string.device_removed_message
        MaintenanceState.isActive(this) || RemoteMaintenanceState.isActive(this) ->
            R.string.maintenance_title to R.string.maintenance_message
        DeviceConfig.hasPendingEnrollment(this) && !DeviceConfig.isEnrolled(this) ->
            R.string.enrolling_title to R.string.enrolling_message
        // Never enrolled and nothing pending: provisioned by a QR without enrollment extras, or
        // a 2.x launcher's prefs - only a new QR scan gets this device onto the device API.
        !DeviceConfig.isEnrolled(this) ->
            R.string.device_removed_title to R.string.device_removed_message
        else -> null
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
