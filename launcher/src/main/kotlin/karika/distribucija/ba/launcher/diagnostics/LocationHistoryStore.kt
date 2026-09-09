package karika.distribucija.ba.launcher.diagnostics

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local queue of GPS fixes sampled every ~15 min by LocationSampleWorker - flushed (and only then
 * cleared) as a batch on the next heartbeat that actually succeeds, so a location fix never has
 * to wake the network by itself, and a heartbeat that fails to send leaves the queue intact for
 * the next attempt instead of losing it.
 */
object LocationHistoryStore {
    private const val PREFS = "location_history"
    private const val KEY_POINTS = "points"

    // ~12h of samples at the 15-min cadence - a heartbeat that keeps failing shouldn't grow this
    // without bound; drop the oldest points first and keep the most recent trail.
    private const val MAX_POINTS = 50

    fun add(context: Context, lat: Double, lon: Double, timestampIso: String) {
        val prefs = prefs(context)
        val points = readAll(prefs)
        points.put(JSONObject().put("lat", lat).put("lon", lon).put("ts", timestampIso))
        val trimmed = if (points.length() > MAX_POINTS) {
            JSONArray().apply {
                for (i in (points.length() - MAX_POINTS) until points.length()) put(points.get(i))
            }
        } else {
            points
        }
        prefs.edit().putString(KEY_POINTS, trimmed.toString()).apply()
    }

    fun readAll(context: Context): JSONArray = readAll(prefs(context))

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_POINTS).apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun readAll(prefs: SharedPreferences): JSONArray {
        val raw = prefs.getString(KEY_POINTS, null) ?: return JSONArray()
        return runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
    }
}
