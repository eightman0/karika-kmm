package karika.distribucija.ba.launcher.update

import android.content.Context
import karika.distribucija.ba.launcher.provision.DeviceConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Plain-HTTPS client for the Laravel device API (/api/V1/device-management). The host comes from
 * the QR's api_base_url (see DeviceConfig), never hardcoded, and every call except enroll carries
 * `Authorization: Bearer <deviceToken>`. The signed APK download URLs from fetchLatestVersion()
 * are fetched by ApkDownloader without any of this - the signature is their credential.
 *
 * Uses connectTimeout/readTimeout directly on the connection rather than relying on the caller's
 * withTimeout() - HttpURLConnection blocks the calling thread on a real socket read, and coroutine
 * cancellation does not interrupt a plain blocking call like that, only a genuinely suspending one.
 */
object DeviceApi {
    private const val TIMEOUT_MS = 15_000

    /** A 50 MiB log zip over a slow LAN won't make it in the JSON calls' 15 s. */
    private const val UPLOAD_READ_TIMEOUT_MS = 5 * 60_000

    data class Enrollment(val deviceToken: String, val apiBaseUrl: String, val app: String?)

    suspend fun enroll(
        apiBaseUrl: String,
        enrollmentToken: String,
        deviceId: String,
        launcherVersionCode: Long,
        launcherVersionName: String,
        androidSdkInt: Int,
        androidRelease: String,
        deviceModel: String
    ): Enrollment = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("enrollmentToken", enrollmentToken)
            .put("deviceId", deviceId)
            .put("launcherVersionCode", launcherVersionCode)
            .put("launcherVersionName", launcherVersionName)
            .put("androidSdkInt", androidSdkInt)
            .put("androidRelease", androidRelease)
            .put("deviceModel", deviceModel)
        val json = request("$apiBaseUrl/enroll", "POST", bearer = null, body = body)
        Enrollment(
            deviceToken = json.getString("deviceToken"),
            apiBaseUrl = json.optString("apiBaseUrl").ifBlank { apiBaseUrl },
            app = json.optString("app").ifBlank { null }
        )
    }

    /** Throws DeviceApiException with code app_not_assigned when this device may not ask for
     * [app] - terminal for that app, don't retry it. */
    suspend fun fetchLatestVersion(context: Context, app: String): KioskVersion = withContext(Dispatchers.IO) {
        val json = authorized(context, "GET", "/version?app=${URLEncoder.encode(app, "UTF-8")}")
        KioskVersion(
            versionCode = json.optString("version_code", "0").toLongOrNull() ?: 0L,
            versionName = json.optString("version_name", ""),
            apkUrl = json.optString("apk_url", ""),
            apkSha256 = json.optString("apk_sha256", ""),
            mandatory = json.optString("mandatory", "true") == "true"
        )
    }

    suspend fun reportHeartbeat(
        context: Context,
        installedPackage: String,
        installedVersionCode: Long,
        installedVersionName: String,
        androidSdkInt: Int,
        androidRelease: String,
        deviceModel: String,
        fcmToken: String?,
        maintenanceActive: Boolean,
        batteryLevel: Int?,
        batteryCharging: Boolean?,
        locations: JSONArray,
        launcherVersionCode: Long,
        launcherVersionName: String
    ) = withContext(Dispatchers.IO) {
        // installedVersionName is the one key the server requires even when it has no value -
        // it's always a (possibly empty) String here, so JSONObject never drops it the way it
        // drops a key put() with null.
        val body = JSONObject()
            .put("installedPackage", installedPackage)
            .put("installedVersionCode", installedVersionCode)
            .put("installedVersionName", installedVersionName)
            .put("androidSdkInt", androidSdkInt)
            .put("androidRelease", androidRelease)
            .put("deviceModel", deviceModel)
            .put("fcmToken", fcmToken)
            .put("maintenanceActive", maintenanceActive)
            .put("batteryLevel", batteryLevel)
            .put("batteryCharging", batteryCharging)
            .put("locations", locations)
            .put("launcherVersionCode", launcherVersionCode)
            .put("launcherVersionName", launcherVersionName)
        authorized(context, "POST", "/heartbeat", body)
    }

    suspend fun reportLoginEvent(context: Context, email: String, timestamp: String?) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("email", email)
                .put("timestamp", timestamp)
            authorized(context, "POST", "/login-event", body)
        }

    suspend fun reportCommandAck(context: Context, command: String, requestId: String?, status: String, message: String?) =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("command", command)
                .put("requestId", requestId)
                .put("status", status)
                .put("message", message?.take(1000))
            authorized(context, "POST", "/command-ack", body)
        }

    suspend fun uploadLogs(context: Context, zip: File, requestId: String?, requestedAt: String?) =
        withContext(Dispatchers.IO) {
            authorizedUpload(context, "/uploads/logs", zip, mapOf("requestId" to requestId, "requestedAt" to requestedAt))
        }

    suspend fun uploadAnalytics(context: Context, zip: File, requestId: String?) =
        withContext(Dispatchers.IO) {
            authorizedUpload(context, "/uploads/analytics", zip, mapOf("requestId" to requestId))
        }

    private fun authorized(context: Context, method: String, path: String, body: JSONObject? = null): JSONObject {
        val (baseUrl, token) = credentials(context)
        return handleDeviceTokenErrors(context) { request("$baseUrl$path", method, token, body) }
    }

    private fun authorizedUpload(context: Context, path: String, file: File, fields: Map<String, String?>): JSONObject {
        val (baseUrl, token) = credentials(context)
        return handleDeviceTokenErrors(context) { multipart("$baseUrl$path", token, file, fields) }
    }

    private fun credentials(context: Context): Pair<String, String> {
        if (!DeviceConfig.isEnrolled(context)) throw DeviceNotEnrolledException()
        val baseUrl = DeviceConfig.apiBaseUrl(context) ?: throw DeviceNotEnrolledException()
        val token = DeviceConfig.deviceToken(context) ?: throw DeviceNotEnrolledException()
        return baseUrl to token
    }

    /** device_token_invalid is terminal for every caller alike, so it's handled once here rather
     * than in each worker: the next call (and every periodic one) then sees isEnrolled() false. */
    private inline fun handleDeviceTokenErrors(context: Context, call: () -> JSONObject): JSONObject = try {
        call()
    } catch (e: DeviceApiException) {
        if (e.code == DeviceApiException.DEVICE_TOKEN_INVALID) {
            DeviceConfig.requireReprovisioning(context, DeviceConfig.ReprovisionReason.DEVICE_REMOVED)
        }
        throw e
    }

    private fun request(url: String, method: String, bearer: String?, body: JSONObject?): JSONObject {
        val connection = openConnection(url, method, bearer, TIMEOUT_MS)
        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }
        }
        return connection.readResponse()
    }

    private fun multipart(url: String, bearer: String, file: File, fields: Map<String, String?>): JSONObject {
        val boundary = "KarikaLauncher" + UUID.randomUUID().toString().replace("-", "")
        val head = buildString {
            fields.forEach { (name, value) ->
                if (value == null) return@forEach
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                append(value).append("\r\n")
            }
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n")
            append("Content-Type: application/zip\r\n\r\n")
        }.toByteArray(StandardCharsets.UTF_8)
        val tail = "\r\n--$boundary--\r\n".toByteArray(StandardCharsets.UTF_8)

        val connection = openConnection(url, "POST", bearer, UPLOAD_READ_TIMEOUT_MS)
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        // Fixed length rather than chunked: PHP behind some web servers can't take a chunked body,
        // and it keeps HttpURLConnection from buffering the whole zip in memory.
        connection.setFixedLengthStreamingMode(head.size + file.length() + tail.size)
        connection.outputStream.use { out ->
            out.write(head)
            file.inputStream().use { it.copyTo(out) }
            out.write(tail)
        }
        return connection.readResponse()
    }

    private fun openConnection(url: String, method: String, bearer: String?, readTimeoutMs: Int): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = readTimeoutMs
        // Without it a Laravel error can come back as an HTML page instead of the error JSON.
        connection.setRequestProperty("Accept", "application/json")
        if (bearer != null) connection.setRequestProperty("Authorization", "Bearer $bearer")
        return connection
    }

    private fun HttpURLConnection.readResponse(): JSONObject {
        try {
            val code = responseCode
            val stream = if (code in 200..299) inputStream else errorStream
            val text = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw DeviceApiException.from(code, url.path, text, getHeaderField("Retry-After"))
            }
            return if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            disconnect()
        }
    }
}

/** The device API's `{ "error": { "code", "message" } }` - branch on [code], not the message. */
class DeviceApiException(
    val httpStatus: Int,
    val code: String,
    message: String,
    val retryAfterSeconds: Long?
) : IOException(message) {

    /** Worth trying the same call again later - rate limit or a server-side failure. Everything
     * else 4xx means the call itself won't succeed as-is. */
    val isRetryable: Boolean get() = httpStatus == 429 || httpStatus >= 500

    companion object {
        const val ENROLLMENT_TOKEN_INVALID = "enrollment_token_invalid"
        const val ENROLLMENT_TOKEN_EXPIRED = "enrollment_token_expired"
        const val ENROLLMENT_TOKEN_EXHAUSTED = "enrollment_token_exhausted"
        const val DEVICE_TOKEN_INVALID = "device_token_invalid"
        const val APP_NOT_ASSIGNED = "app_not_assigned"

        fun from(httpStatus: Int, path: String, body: String, retryAfter: String?): DeviceApiException {
            val error = runCatching { JSONObject(body).optJSONObject("error") }.getOrNull()
            val code = error?.optString("code")?.ifBlank { null } ?: "http_$httpStatus"
            val message = error?.optString("message")?.ifBlank { null } ?: body.take(200)
            return DeviceApiException(
                httpStatus,
                code,
                "HTTP $httpStatus $code from $path: $message",
                retryAfter?.trim()?.toLongOrNull()
            )
        }
    }
}

/** No device token yet (enrollment still pending) or not any more (re-provisioning required) -
 * nothing to call the API with. */
class DeviceNotEnrolledException : IOException("Device is not enrolled")
