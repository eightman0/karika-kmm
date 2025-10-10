package karika.distribucija.ba.provision

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
import karika.distribucija.ba.MainActivityKiosk
import karika.distribucija.ba.utils.KioskMode

class KarikaKiosk(private val context: ComponentActivity) : KioskMode {
    private var adminComponentName: ComponentName =
        KarikaDeviceAdminReceiver.getReceiverComponentName(context)
    private var devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private fun enterKiosk() {
        if (isAdmin()) {
            setKioskPolicies(true, isAdmin())
        }
    }

    private fun exitKiosk() {
        devicePolicyManager.removeActiveAdmin(adminComponentName)
        setKioskPolicies(false, isAdmin())
    }

    private fun setKioskPolicies(enable: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            setRestrictions(enable)
            enableStayOnWhilePluggedIn(enable)
            setUpdatePolicy(enable)
            setAsHomeApp(enable)
            setKeyGuardEnabled(enable)
        }
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            context.packageName,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            context.packageName,
            Manifest.permission.KILL_BACKGROUND_PROCESSES,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            context.packageName,
            Manifest.permission.CAMERA,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
        devicePolicyManager.setPermissionGrantState(
            adminComponentName,
            context.packageName,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
        )
        listOf(
            "com.google.android.apps.youtube.music",
            "com.android.chrome",
            "com.android.phone",
            "com.android.fmradio",
            "com.google.android.keep",
            "com.google.android.apps.photos",
            "com.google.android.calendar",
            "com.google.android.apps.docs",
            "com.google.android.apps.maps",
            "com.android.providers.calendar",
            "com.android.soundrecorder",
            "com.google.android.apps.googleassistant",
            "com.google.android.apps.nbu.files",
            "com.google.android.calculator",
            "com.google.android.gm",
            "com.google.android.apps.droidtalk",
            "com.google.android.youtube",
            "com.android.deskclock",
            "com.google.android.marvin.talkback",
            "com.google.android.googlequicksearchbox",
            "com.google.android.deskclock",
            "com.google.android.apps.tachyon"
        ).forEach {
            devicePolicyManager.setApplicationHidden(
                adminComponentName,
                it,
                true
            )
        }
        devicePolicyManager.setApplicationHidden(
            adminComponentName,
            "com.mediatek.camera",
            false
        )
        setLockTask(enable, isAdmin)
    }

    private fun setRestrictions(disallow: Boolean) {
        // to show status bar
        devicePolicyManager.setLockTaskFeatures(
            adminComponentName,
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
        )

        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, disallow)
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, disallow)
        setUserRestriction(UserManager.DISALLOW_ADD_USER, disallow)
        setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, disallow)
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, false)
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

    private fun setLockTask(start: Boolean, isAdmin: Boolean) {
        if (isAdmin) {
            devicePolicyManager.setLockTaskPackages(
                adminComponentName,
                if (start) arrayOf(
                    context.packageName,
                    "com.android.settings",
                    "com.mediatek.camera",
                    "com.android.vending",
                    "com.google.android.packageinstaller",
                    "com.google.android.gms",
                    "com.google.android.gsf",
                    "com.android.packageinstaller"
                ) else arrayOf()
            )
        }
        if (start) {
            context.startLockTask()
        } else {
            context.stopLockTask()
        }
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
                ComponentName(context.packageName, MainActivityKiosk::class.java.name)
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

    override fun enter() {
        enterKiosk()
    }

    override fun exit() {
        exitKiosk()
    }

    override fun isAdmin() = devicePolicyManager.isDeviceOwnerApp(context.packageName)
}