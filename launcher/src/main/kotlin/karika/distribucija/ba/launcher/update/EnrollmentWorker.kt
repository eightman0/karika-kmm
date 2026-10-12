package karika.distribucija.ba.launcher.update

import android.content.Context
import android.os.Build
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.diagnostics.DeviceHeartbeat
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import karika.distribucija.ba.launcher.provision.DeviceConfig
import karika.distribucija.ba.logging.AppLogger
import java.util.concurrent.TimeUnit

/**
 * Trades the QR's enrollment token for this device's own device token (POST /enroll), retrying
 * with backoff until it gets a 201 or a terminal enrollment error. Enqueued from
 * DeviceConfig.readFromProvisioningIntent() right after provisioning, and again from LauncherApp
 * on every process start while an enroll is still pending, so a process kill mid-flight doesn't
 * leave the device stuck un-enrolled.
 *
 * Enrolling the same ANDROID_ID again is safe: the server revokes the older device token and
 * returns a new one, which is what gets kept here.
 */
class EnrollmentWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val enrollmentToken = DeviceConfig.enrollmentToken(context)
        val apiBaseUrl = DeviceConfig.apiBaseUrl(context)
        if (enrollmentToken == null || apiBaseUrl == null || DeviceConfig.reprovisionReason(context) != null) {
            AppLogger.i(TAG, "Nothing to enroll")
            return Result.success()
        }

        val deviceId = DeviceIdentity.id(context)
        val (launcherVersionCode, launcherVersionName) = DeviceHeartbeat.installedVersion(context, context.packageName)
        AppLogger.i(TAG, "Enrolling $deviceId against $apiBaseUrl (attempt ${runAttemptCount + 1})")
        return try {
            val enrollment = DeviceApi.enroll(
                apiBaseUrl = apiBaseUrl,
                enrollmentToken = enrollmentToken,
                deviceId = deviceId,
                launcherVersionCode = launcherVersionCode,
                launcherVersionName = launcherVersionName,
                androidSdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE,
                deviceModel = Build.MODEL
            )
            DeviceConfig.completeEnrollment(context, enrollment.deviceToken, enrollment.apiBaseUrl, enrollment.app)
            AppLogger.i(TAG, "Enrolled $deviceId, app=${enrollment.app} api=${enrollment.apiBaseUrl}")
            UpdateScheduler.scheduleDeviceApiWork(context)
            // First heartbeat and, on a fresh device, the first payload install - no reason to
            // wait out the periodic interval for either.
            UpdateScheduler.triggerImmediateCheck(context)
            Result.success()
        } catch (e: DeviceApiException) {
            when {
                e.code in TERMINAL_CODES -> {
                    AppLogger.e(TAG, "Enrollment rejected: ${e.code}")
                    DeviceConfig.requireReprovisioning(context, DeviceConfig.ReprovisionReason.QR_INVALID)
                    Result.failure()
                }
                e.isRetryable -> {
                    AppLogger.w(TAG, "Enrollment failed (${e.code}), retrying with backoff")
                    Result.retry()
                }
                else -> {
                    // 422 and the like - the request body itself is wrong, sending it again won't
                    // change the answer. Retried on the next process start (see LauncherApp).
                    AppLogger.e(TAG, "Enrollment failed: ${e.message}")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "Enrollment failed: ${e.message}, retrying with backoff")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "EnrollmentWorker"
        private const val WORK_NAME = "device_enrollment"

        private val TERMINAL_CODES = setOf(
            DeviceApiException.ENROLLMENT_TOKEN_INVALID,
            DeviceApiException.ENROLLMENT_TOKEN_EXPIRED,
            DeviceApiException.ENROLLMENT_TOKEN_EXHAUSTED
        )

        /** KEEP, not REPLACE: both provisioning checkpoints call this within moments of each
         * other, and replacing an enroll already in flight could throw away the 201 it gets back
         * (costing another enrollment use when the profile has a limit). */
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<EnrollmentWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                // 30 s doubling, which also covers enroll's 429 Retry-After (its limit is per minute).
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }
    }
}
