package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONArray

/**
 * Pulls the GPS fixes salesrep has queued locally (see its own LocationHistoryStore) through the
 * same cross-process content URI bridge already used for logs - sampling moved there because
 * doing it from this Device Owner process was causing ANRs on real devices. Salesrep just keeps a
 * bounded, unconsumed window of recent points (it has no way to know whether a heartbeat actually
 * reached the server), so dedup happens here instead: remember the timestamp of the last point
 * actually included in a successful send, the same "trust the artifact, not a flag" spirit as
 * InstalledApkState.
 */
object LocationHistoryReader {
    private const val TAG = "LocationHistoryReader"
    private const val LOCATIONS_URI = "content://karika.distribucija.ba.salesrep.logs/locations"
    private const val PREFS = "location_history_cursor"
    private const val KEY_LAST_SENT_TS = "last_sent_ts"

    fun readNewPoints(context: Context): JSONArray {
        val raw = runCatching {
            context.contentResolver.openInputStream(Uri.parse(LOCATIONS_URI))
                ?.use { it.bufferedReader().readText() }
        }.onFailure { Log.w(TAG, "Could not read salesrep locations: ${it.message}") }.getOrNull()
            ?: return JSONArray()

        val points = runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
        val lastSent = lastSentTs(context) ?: return points
        val fresh = JSONArray()
        for (i in 0 until points.length()) {
            val point = points.getJSONObject(i)
            if (point.getString("ts") > lastSent) fresh.put(point)
        }
        return fresh
    }

    /** Call only once the heartbeat carrying these points has actually been sent successfully -
     * same ordering rule DeviceHeartbeat already follows for clearing the old local queue. */
    fun markSent(context: Context, points: JSONArray) {
        if (points.length() == 0) return
        val last = points.getJSONObject(points.length() - 1).getString("ts")
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_LAST_SENT_TS, last)
            .apply()
    }

    private fun lastSentTs(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST_SENT_TS, null)
}
