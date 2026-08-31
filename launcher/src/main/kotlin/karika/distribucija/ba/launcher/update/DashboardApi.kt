package karika.distribucija.ba.launcher.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Plain-HTTPS client for the admin dashboard's device API - replaces direct Firestore reads/
 * writes. Firestore's client is gRPC-only on both this app and the dashboard's Python side, with
 * no REST fallback on either - and gRPC turned out to be exactly what a bad network moment
 * blocks while plain HTTPS keeps working (confirmed live: the Firebase console, which talks
 * REST/WebChannel, kept working throughout an outage that hung every Firestore call here).
 *
 * Uses connectTimeout/readTimeout directly on the connection rather than relying on the caller's
 * withTimeout() - HttpURLConnection blocks the calling thread on a real socket read, and coroutine
 * cancellation does not interrupt a plain blocking call like that, only a genuinely suspending one.
 */
object DashboardApi {
    private const val BASE_URL = "https://karika.car4hire.ba"
    private const val TIMEOUT_MS = 15_000

    suspend fun fetchLatestVersion(deviceId: String): KioskVersion = withContext(Dispatchers.IO) {
        val encodedId = URLEncoder.encode(deviceId, "UTF-8")
        val json = get("$BASE_URL/api/version?device_id=$encodedId")
        KioskVersion(
            versionCode = json.optString("version_code", "0").toLongOrNull() ?: 0L,
            versionName = json.optString("version_name", ""),
            apkUrl = json.optString("apk_url", ""),
            apkSha256 = json.optString("apk_sha256", ""),
            mandatory = json.optString("mandatory", "true") == "true"
        )
    }

    suspend fun reportHeartbeat(
        deviceId: String,
        installedPackage: String,
        installedVersionCode: Long,
        installedVersionName: String,
        androidSdkInt: Int,
        androidRelease: String,
        deviceModel: String,
        fcmToken: String?,
        maintenanceActive: Boolean
    ) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("installedPackage", installedPackage)
            .put("installedVersionCode", installedVersionCode)
            .put("installedVersionName", installedVersionName)
            .put("androidSdkInt", androidSdkInt)
            .put("androidRelease", androidRelease)
            .put("deviceModel", deviceModel)
            .put("fcmToken", fcmToken)
            .put("maintenanceActive", maintenanceActive)
        post("$BASE_URL/api/devices/$deviceId/heartbeat", body)
    }

    suspend fun reportLogUploaded(deviceId: String, url: String, path: String, requestedAt: String?) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("url", url)
                .put("path", path)
                .put("requestedAt", requestedAt)
            post("$BASE_URL/api/devices/$deviceId/log-uploaded", body)
        }

    suspend fun reportAnalyticsUploaded(deviceId: String, url: String, path: String) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("url", url)
                .put("path", path)
            post("$BASE_URL/api/devices/$deviceId/analytics-uploaded", body)
        }

    suspend fun reportLoginEvent(deviceId: String, email: String, timestamp: String?) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("email", email)
                .put("timestamp", timestamp)
            post("$BASE_URL/api/devices/$deviceId/login-event", body)
        }

    suspend fun reportDeviceMapping(deviceId: String, customerId: String?, siteId: String?) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("customerId", customerId)
                .put("siteId", siteId)
            post("$BASE_URL/api/devices/$deviceId/mapping", body)
        }

    suspend fun reportCommandAck(deviceId: String, command: String, requestId: String?, status: String, message: String?) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("command", command)
                .put("requestId", requestId)
                .put("status", status)
                .put("message", message)
            post("$BASE_URL/api/devices/$deviceId/command-ack", body)
        }

    private fun get(url: String): JSONObject {
        val connection = openConnection(url, "GET")
        return connection.readResponse()
    }

    private fun post(url: String, body: JSONObject): JSONObject {
        val connection = openConnection(url, "POST")
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
        return connection.readResponse()
    }

    private fun openConnection(url: String, method: String): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        return connection
    }

    private fun HttpURLConnection.readResponse(): JSONObject {
        try {
            val code = responseCode
            val stream = if (code in 200..299) inputStream else errorStream
            val text = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw java.io.IOException("HTTP $code from $url: $text")
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            disconnect()
        }
    }
}
