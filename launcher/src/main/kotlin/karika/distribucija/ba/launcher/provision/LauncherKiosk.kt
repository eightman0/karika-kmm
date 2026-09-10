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
        setScreenTimeout(enable)
        setUpdatePolicy(enable)
        setAsHomeApp(enable)
        setKeyGuardEnabled(enable)
        // Only this one, targeting this app's own package - it is not a "dangerous" runtime
        // permission, so unlike ACCESS_FINE_LOCATION/CAMERA (see below for why those are no
        // longer granted this way at all) it never touched the broken notifier code path.
        grantPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES, context.packageName)
        // ACCESS_FINE_LOCATION/ACCESS_COARSE_LOCATION/CAMERA used to be silently granted to
        // salesrep here via DevicePolicyManager.setPermissionGrantState() - that specific DPM call
        // is what invokes PermissionController's AutoGrantPermissionsNotifier, and on this device
        // (Android 12/API 31) that notifier itself is broken (IllegalArgumentException, missing
        // FLAG_IMMUTABLE/FLAG_MUTABLE on a PendingIntent - an OS/OEM bug, not ours, confirmed
        // against AOSP source and with no in-app workaround: install-time grants require a
        // signature|installer permission a normal Device Owner app cannot hold either). Confirmed
        // by direct observation too - a build from before these three grants existed did not
        // crash, only builds that called setPermissionGrantState() for a dangerous permission did.
        // Salesrep now requests these three itself, as a normal runtime permission dialog (see
        // SalesRepApp/AttachmentPicker) - that goes through a completely different PermissionController
        // code path than the DPM-driven one that crashes, and does not need any lock-task
        // workaround either, since salesrep is always the current lock-task-allowed foreground app
        // when it asks (unlike the ADB-authorization or battery-optimization dialogs, which needed
        // a DIFFERENT app's system UI to draw over the launcher's own pinned activity).
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

    /** 5 min while active, not indefinite - long enough that a customer/employee glancing at it
     * mid-use never sees it go dark (STAY_ON_WHILE_PLUGGED_IN only helps while actually charging),
     * short enough that a genuinely idle kiosk still lets its screen sleep (helps with OLED
     * burn-in over a long deployment). Doze can kick in sooner once the screen does sleep - and on
     * this OEM's build, NEITHER app ends up on the Doze/battery-optimization whitelist on its own,
     * Device Owner or not (confirmed via `adb shell dumpsys deviceidle whitelist` coming back
     * without either package listed at all, on a real device - the "Device Owner apps are
     * automatically exempt" claim in AOSP docs does not hold here). That is what was actually
     * behind a real device going an hour with no heartbeat and no push getting through - not just
     * salesrep's gap this comment used to call out alone. Neither app has a working in-app way to
     * self-exempt without either the interactive Settings dialog (dropped for salesrep - see git
     * history for the PermissionController/crash history, unrelated to this specific dialog but
     * still not brought back by choice) or a manual step during provisioning:
     * `adb shell dumpsys deviceidle whitelist +karika.distribucija.ba.launcher` and the same for
     * `...salesrep`, is the only way to cover this gap for now, for both packages. */
    private fun setScreenTimeout(enable: Boolean) {
        devicePolicyManager.setSystemSetting(
            adminComponentName,
            Settings.System.SCREEN_OFF_TIMEOUT,
            if (enable) KIOSK_SCREEN_OFF_TIMEOUT_MS.toString() else DEFAULT_SCREEN_OFF_TIMEOUT_MS.toString()
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
        const val KIOSK_SCREEN_OFF_TIMEOUT_MS = 5 * 60_000

        // Android's own out-of-the-box default, restored on exit() so leaving kiosk mode doesn't
        // leave the screen timeout stuck at the kiosk value forever.
        const val DEFAULT_SCREEN_OFF_TIMEOUT_MS = 30_000
    }
}
