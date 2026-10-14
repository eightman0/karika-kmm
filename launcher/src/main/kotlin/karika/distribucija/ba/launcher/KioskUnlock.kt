package karika.distribucija.ba.launcher

import android.app.admin.DevicePolicyManager
import android.content.Context
import karika.distribucija.ba.launcher.provision.LauncherDeviceAdminReceiver

/** Temporarily takes the device out of lock task (RemoteDebugUnlock's 15 minutes, then it
 * re-locks on its own). Shared by the remote debug_unlock command and the on-device service PIN. */
object KioskUnlock {
    fun unlock(context: Context) {
        RemoteDebugUnlock.begin(context)
        // Setting the flag alone only takes effect the next time LauncherActivity resumes on its
        // own - could be indefinite with the payload sitting in front. Force lock task off
        // immediately: removing the current package from the allowlist is documented to stop
        // lock task mode right away, regardless of which activity holds the pin.
        val devicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
        val admin = LauncherDeviceAdminReceiver.getReceiverComponentName(context)
        devicePolicyManager.setLockTaskPackages(admin, arrayOf())
    }

    /** Back into kiosk before the 15 minutes are up. */
    fun relock(context: Context) {
        RemoteDebugUnlock.end(context)
        LauncherActivity.bringToFront(context)
    }
}
