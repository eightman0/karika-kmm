package karika.distribucija.ba.salesrep.diagnostics

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Local queue of GPS fixes sampled every ~15 min by LocationSampleWorker - a plain JSON file, same
 * idea as AppLogger's log files, that the launcher pulls through LogProvider's content URI on
 * every heartbeat, the same bridge it already uses to pull this app's logs/analytics. Sampling
 * moved here from the launcher itself: doing it from that Device Owner background process was
 * causing ANRs on real devices, while salesrep is normally the app actually in the foreground.
 */
object LocationHistoryStore {
    private const val DIR_NAME = "diagnostics"
    private const val FILE_NAME = "location_history.json"

    // ~12h of samples at the 15-min cadence - keeps the file bounded even if the launcher goes a
    // long time without pulling it.
    private const val MAX_POINTS = 50

    private var file: File? = null

    /** Call once, early in Application.onCreate(), same as AppLogger.init(). */
    fun init(context: Context) {
        val dir = File(context.getExternalFilesDir(null), DIR_NAME).apply { mkdirs() }
        file = File(dir, FILE_NAME)
    }

    fun add(lat: Double, lon: Double, timestampIso: String) {
        val target = file ?: return
        val points = readAll(target)
        points.put(JSONObject().put("lat", lat).put("lon", lon).put("ts", timestampIso))
        val trimmed = if (points.length() > MAX_POINTS) {
            JSONArray().apply {
                for (i in (points.length() - MAX_POINTS) until points.length()) put(points.get(i))
            }
        } else {
            points
        }
        runCatching { target.writeText(trimmed.toString()) }
    }

    /** The file a ContentProvider bridge (see LogProvider) serves to the launcher. */
    fun currentFile(): File? = file

    private fun readAll(target: File): JSONArray {
        if (!target.exists()) return JSONArray()
        val raw = runCatching { target.readText() }.getOrNull() ?: return JSONArray()
        return runCatching { JSONArray(raw) }.getOrDefault(JSONArray())
    }
}
