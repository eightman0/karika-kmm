package karika.distribucija.ba.launcher.update

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import karika.distribucija.ba.launcher.diagnostics.DeviceIdentity

/** Reports the customer_id/site_id read from the QR provisioning payload (see
 * ProvisioningSuccessActivity/DeviceMapping) to the dashboard, once. */
class DeviceMappingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val customerId = inputData.getString(KEY_CUSTOMER_ID)
        val siteId = inputData.getString(KEY_SITE_ID)
        return try {
            DashboardApi.reportDeviceMapping(DeviceIdentity.id(applicationContext), customerId, siteId)
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to report device mapping", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DeviceMappingWorker"
        const val KEY_CUSTOMER_ID = "customerId"
        const val KEY_SITE_ID = "siteId"
    }
}
