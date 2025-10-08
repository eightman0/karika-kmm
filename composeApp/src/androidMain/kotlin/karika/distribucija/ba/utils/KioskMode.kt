package karika.distribucija.ba.utils

import com.google.android.play.core.appupdate.AppUpdateManager
import karika.distribucija.ba.MainActivity

interface KioskMode {
    fun enter()
    fun exit()
    fun isAdmin(): Boolean
    fun pauseKioskMode(activity: MainActivity, appUpdateManager: AppUpdateManager)
}