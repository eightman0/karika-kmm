package karika.distribucija.ba.launcher.provision

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES

class LauncherDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        if (Util.SDK_INT >= VERSION_CODES.O) {
            // See http://b/177617306.
            return
        }
        val task = PostProvisioningTask(context)
        if (task.performPostProvisioningOperations()) {
            return
        }

        val launchIntent = task.postProvisioningLaunchIntent
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        }
    }

    companion object {
        @JvmStatic
        fun getComponentName(context: Context): ComponentName? {
            return if (Util.isDeviceOwner(context) || Util.isProfileOwner(context)) {
                getReceiverComponentName(context)
            } else {
                null
            }
        }

        fun getReceiverComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, LauncherDeviceAdminReceiver::class.java)
        }
    }
}
