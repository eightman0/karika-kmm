package karika.distribucija.ba.launcher.provision

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.UserManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import karika.distribucija.ba.launcher.KnownApps
import karika.distribucija.ba.launcher.LauncherActivity
import karika.distribucija.ba.launcher.RemoteDebugUnlock

class LauncherKiosk(private val context: ComponentActivity) {
    private var adminComponentName: ComponentName =
        LauncherDeviceAdminReceiver.getReceiverComponentName(context)
    private var devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun isAdmin() = devicePolicyManager.isDeviceOwnerApp(context.packageName)

    fun enter() {
        if (isAdmin()) {
            setKioskPolicies(true)
        }
    }

    fun exit() {
        devicePolicyManager.removeActiveAdmin(adminComponentName)
        setKioskPolicies(false)
    }

    private fun setKioskPolicies(enable: Boolean) {
        setRestrictions(enable)
        enableStayOnWhilePluggedIn(enable)
        disableScreenTimeout(enable)
        setUpdatePolicy(enable)
        setAsHomeApp(enable)
        setKeyGuardEnabled(enable)
        grantPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES, context.packageName)
        // Silently granted to salesrep, not this app - LocationSampleWorker runs there now (moved
        // out of this Device Owner process after it started causing ANRs on real devices). Only
        // the foreground pair, deliberately not ACCESS_BACKGROUND_LOCATION - it's a "dangerous"
        // runtime permission, and granting any of those through Device Owner makes the OS try to
        // notify the user "your admin granted you X" - on this device (Android 12/API 31) that
        // notifier itself is broken (PermissionController: IllegalArgumentException, missing
        // FLAG_IMMUTABLE/FLAG_MUTABLE on a PendingIntent - an OS/OEM bug, not ours) and crashes,
        // which then froze the lock-task-pinned launcher's input, showing up there as an ANR. The
        // grant itself still lands fine despite the crash (confirmed via dumpsys: granted=true,
        // POLICY_FIXED) - grantPermission() below only calls the DPM API once, the first time a
        // permission isn't granted yet, specifically to avoid re-triggering that broken notifier
        // on every single onResume() the way calling it unconditionally did. LocationSampleWorker
        // runs as a brief foreground service, which Android treats as foreground for location
        // access, so it never needs the background grant at all.
        grantPermission(Manifest.permission.ACCESS_FINE_LOCATION, KnownApps.PRIMARY.packageName)
        grantPermission(Manifest.permission.ACCESS_COARSE_LOCATION, KnownApps.PRIMARY.packageName)
        // Also silent, and for the same underlying reason as the location grants above: a normal
        // runtime request for this crashed Permission Controller instead of showing its dialog,
        // since it can't present itself over a lock-task-pinned kiosk activity. Salesrep no longer
        // asks for it at all (see AttachmentPicker.takePhoto()) - it just expects to already have it.
        grantPermission(Manifest.permission.CAMERA, KnownApps.PRIMARY.packageName)
        // A technician plugging in ADB via CMD_DEBUG_UNLOCK needs lock task actually OFF, not just
        // skipped once - every onResume() (screen touch, app switch, anything) calls back in here,
        // so without this check the very next resume would silently re-pin it before they get a
        // chance to authorize the debugger. See RemoteDebugUnlock's own doc comment for why this
        // is time-bound rather than a plain toggle.
        setLockTask(enable && !RemoteDebugUnlock.isActive(context))
    }

    /** Only calls into DPM when the permission isn't already granted - see the long comment above
     * on why calling this unconditionally on every onResume() is actively harmful, not just
     * wasted work: it keeps re-triggering a broken OS notifier on some devices. */
    private fun grantPermission(permission: String, targetPackage: String) {
        val alreadyGranted = context.packageManager.checkPermission(permission, targetPackage) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) return
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            targetPackage,
            permission,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
    }

    private fun setRestrictions(disallow: Boolean) {
        devicePolicyManager.setLockTaskFeatures(
            adminComponentName,
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
        )

        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, false)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
        setUserRestriction(UserManager.DISALLOW_CONFIG_VPN, disallow)
        setUserRestriction(UserManager.DISALLOW_AIRPLANE_MODE, disallow)
        setUserRestriction(UserManager.DISALLOW_CONFIG_PRIVATE_DNS, disallow)
    }

    private fun setUserRestriction(restriction: String, disallow: Boolean) = if (disallow) {
        devicePolicyManager.addUserRestriction(adminComponentName, restriction)
    } else {
        devicePolicyManager.clearUserRestriction(adminComponentName, restriction)
    }

    private fun enableStayOnWhilePluggedIn(active: Boolean) = if (active) {
        devicePolicyManager.setGlobalSetting(
            adminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            (BatteryManager.BATTERY_PLUGGED_AC
                    or BatteryManager.BATTERY_PLUGGED_USB
                    or BatteryManager.BATTERY_PLUGGED_WIRELESS).toString()
        )
    } else {
        devicePolicyManager.setGlobalSetting(
            adminComponentName,
            Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
            "0"
        )
    }

    /** Allowlist covers the launcher itself plus every known payload app, so lock task survives
     * the launcher starting one of them on top of it. */
    private fun setLockTask(start: Boolean) {
        val allowedPackages = listOf(
            context.packageName,
            "com.android.settings",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.google.android.gms",
            "com.google.android.gsf"
        ) + KnownApps.ALL.map { it.packageName }

        devicePolicyManager.setLockTaskPackages(
            adminComponentName,
            if (start) allowedPackages.toTypedArray() else arrayOf()
        )
        if (start) {
            context.startLockTask()
        } else {
            context.stopLockTask()
        }
    }

    /** STAY_ON_WHILE_PLUGGED_IN only helps while charging - an unattended kiosk that's briefly
     * off power (or on a device the emulator doesn't report as "plugged in" at all) would
     * otherwise still hit the normal screen-off timeout and go dark with nothing to wake it,
     * which looks indistinguishable from a dead/hung device to whoever's standing in front of it. */
    private fun disableScreenTimeout(enable: Boolean) {
        devicePolicyManager.setSystemSetting(
            adminComponentName,
            Settings.System.SCREEN_OFF_TIMEOUT,
            if (enable) Int.MAX_VALUE.toString() else DEFAULT_SCREEN_OFF_TIMEOUT_MS.toString()
        )
    }

    private fun setUpdatePolicy(enable: Boolean) {
        if (enable) {
            devicePolicyManager.setSystemUpdatePolicy(
                adminComponentName,
                SystemUpdatePolicy.createWindowedInstallPolicy(60, 120)
            )
        } else {
            devicePolicyManager.setSystemUpdatePolicy(adminComponentName, null)
        }
    }

    private fun setAsHomeApp(enable: Boolean) {
        if (enable) {
            val intentFilter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            devicePolicyManager.addPersistentPreferredActivity(
                adminComponentName,
                intentFilter,
                ComponentName(context.packageName, LauncherActivity::class.java.name)
            )
        } else {
            devicePolicyManager.clearPackagePersistentPreferredActivities(
                adminComponentName, context.packageName
            )
        }
    }

    private fun setKeyGuardEnabled(enable: Boolean) {
        devicePolicyManager.setKeyguardDisabled(adminComponentName, !enable)
    }

    private companion object {
        // Android's own out-of-the-box default, restored on exit() so leaving kiosk mode doesn't
        // leave the screen timeout disabled forever.
        const val DEFAULT_SCREEN_OFF_TIMEOUT_MS = 30_000
    }
}
