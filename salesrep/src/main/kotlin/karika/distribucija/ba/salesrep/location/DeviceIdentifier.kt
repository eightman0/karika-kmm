package karika.distribucija.ba.salesrep.location

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

/** Stable per-install id sent with each employee-location POST - not tied to login, so the
 * backend can identify "this phone" across re-logins on the same device. */
object DeviceIdentifier {
    private const val PREFS = "salesrep_device"
    private const val KEY_DEVICE_ID = "device_id"

    fun deviceId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_DEVICE_ID, null)?.let { return it }
        val id = UUID.randomUUID().toString()
        prefs.edit { putString(KEY_DEVICE_ID, id) }
        return id
    }
}
