package karika.distribucija.ba.provision

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES

object Util {
    private val IS_RUNNING_U = Build.VERSION.CODENAME == "UpsideDownCake"
    val SDK_INT: Int = if (IS_RUNNING_U) VERSION_CODES.CUR_DEVELOPMENT else Build.VERSION.SDK_INT

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = getDevicePolicyManager(context)
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun isProfileOwner(context: Context): Boolean {
        val dpm = getDevicePolicyManager(context)
        return dpm.isProfileOwnerApp(context.packageName)
    }

    private fun getDevicePolicyManager(context: Context): DevicePolicyManager {
        return context.getSystemService(Service.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
}
