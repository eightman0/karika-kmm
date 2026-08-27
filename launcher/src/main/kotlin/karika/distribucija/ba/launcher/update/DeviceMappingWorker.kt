package karika.distribucija.ba.launcher.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity
import karika.distribucija.ba.logging.AppLogger

/** Reports the customer_id/site_id read from the QR provisioning payload (see
 * ProvisioningSuccessActivity/DeviceMapping) to the dashboard, once. */
class DeviceMappingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val customerId = inputData.getString(KEY_CUSTOMER_ID)
        val siteId = inputData.getString(KEY_SITE_ID)
        val deviceId = DeviceIdentity.id(applicationContext)
        AppLogger.i(TAG, "Reporting device mapping for $deviceId: customer_id=$customerId site_id=$siteId")
        return try {
            DashboardApi.reportDeviceMapping(deviceId, customerId, siteId)
            AppLogger.i(TAG, "Device mapping reported successfully for $deviceId")
            Result.success()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to report device mapping for $deviceId", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DeviceMappingWorker"
        const val KEY_CUSTOMER_ID = "customerId"
        const val KEY_SITE_ID = "siteId"
    }
}
