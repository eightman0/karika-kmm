package karika.distribucija.ba.provision

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.os.Build.VERSION_CODES
import android.util.Log
import karika.distribucija.ba.provision.KarikaDeviceAdminReceiver.Companion.getComponentName

class PostProvisioningTask(private val mContext: Context) {
    private val mDevicePolicyManager =
        mContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val mSharedPrefs: SharedPreferences

    init {
        mSharedPrefs = mContext.getSharedPreferences(POST_PROV_PREFS, Context.MODE_PRIVATE)
    }

    fun performPostProvisioningOperations(): Boolean {
        if (isPostProvisioningDone) {
            return true
        }
        markPostProvisioningDone()
        if (Util.SDK_INT >= VERSION_CODES.M) {
            autoGrantRequestedPermissionsToSelf()
        }

        return false
    }

    val postProvisioningLaunchIntent: Intent
        get() {
            val launch =
                Intent(mContext, FinalizeActivity::class.java)
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return launch
        }

    @SuppressLint("ApplySharedPref")
    private fun markPostProvisioningDone() {
        mSharedPrefs.edit()
            .putBoolean(KEY_POST_PROV_DONE, true)
            .commit()
    }

    private val isPostProvisioningDone: Boolean
        get() = mSharedPrefs.getBoolean(KEY_POST_PROV_DONE, false)

    private fun autoGrantRequestedPermissionsToSelf() {
        val packageName = mContext.packageName
        val adminComponentName = getComponentName(mContext)

        val permissions = getRuntimePermissions(mContext.packageManager, packageName)
        for (permission in permissions) {
            mDevicePolicyManager.setPermissionGrantState(
                adminComponentName,
                packageName,
                permission,
                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
            )
        }
    }

    private fun getRuntimePermissions(
        packageManager: PackageManager,
        packageName: String
    ): List<String> {
        val permissions: MutableList<String> = ArrayList()
        val packageInfo: PackageInfo?
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        } catch (e: PackageManager.NameNotFoundException) {
            return permissions
        }

        if (packageInfo?.requestedPermissions != null) {
            for (requestedPerm in packageInfo.requestedPermissions!!) {
                if (isRuntimePermission(packageManager, requestedPerm)) {
                    permissions.add(requestedPerm)
                }
            }
        }
        return permissions
    }

    private fun isRuntimePermission(packageManager: PackageManager, permission: String): Boolean {
        try {
            val pInfo = packageManager.getPermissionInfo(permission, 0)
            if (pInfo != null) {
                if ((pInfo.protectionLevel and PermissionInfo.PROTECTION_MASK_BASE)
                    == PermissionInfo.PROTECTION_DANGEROUS
                ) {
                    return true
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.i(
                TAG,
                "Could not retrieve info about the permission: $permission"
            )
        }
        return false
    }

    companion object {
        private const val TAG = "PostProvisioningTask"
        private const val POST_PROV_PREFS = "post_prov_prefs"
        private const val KEY_POST_PROV_DONE = "key_post_prov_done"
    }
}
