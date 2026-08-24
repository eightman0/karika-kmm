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
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            context.packageName,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
        // So the keep-alive foreground service's notification never blocks on a runtime prompt.
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            context.packageName,
            Manifest.permission.POST_NOTIFICATIONS,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
        setLockTask(enable)
    }

    private fun setRestrictions(disallow: Boolean) {
        devicePolicyManager.setLockTaskFeatures(
            adminComponentName,
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
        )

        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
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
