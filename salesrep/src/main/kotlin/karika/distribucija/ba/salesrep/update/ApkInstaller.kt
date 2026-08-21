package karika.distribucija.ba.salesrep.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream

/**
 * Installs an already-downloaded, checksum-verified APK with no user prompt, relying on the
 * caller being Device Owner: INSTALL_REASON_POLICY + USER_ACTION_NOT_REQUIRED (API 31+; on
 * Device Owner devices below that, PackageInstaller already skips confirmation without this
 * call) are what suppress the install dialog.
 */
object ApkInstaller {
    private const val TAG = "ApkInstaller"
    private const val ACTION_INSTALL_RESULT = "karika.distribucija.ba.salesrep.update.INSTALL_RESULT"

    suspend fun install(context: Context, apkFile: File): Boolean = suspendCancellableCoroutine { cont ->
        val packageInstaller = context.packageManager.packageInstaller
        val sessionId: Int
        val receiver: BroadcastReceiver

        try {
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setInstallReason(PackageManager.INSTALL_REASON_POLICY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
            }
            sessionId = packageInstaller.createSession(params)
        } catch (e: Exception) {
            Log.e(TAG, "Could not create install session", e)
            cont.resumeWith(Result.success(false))
            return@suspendCancellableCoroutine
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                if (intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1) != sessionId) return
                runCatching { receiverContext.unregisterReceiver(this) }
                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                if (status == PackageInstaller.STATUS_SUCCESS) {
                    Log.i(TAG, "Install succeeded")
                } else {
                    Log.e(TAG, "Install failed: ${statusName(status)} ($status), message=$message")
                }
                if (cont.isActive) cont.resumeWith(Result.success(status == PackageInstaller.STATUS_SUCCESS))
            }
        }
        val filter = IntentFilter(ACTION_INSTALL_RESULT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }

        cont.invokeOnCancellation {
            runCatching { context.unregisterReceiver(receiver) }
        }

        try {
            packageInstaller.openSession(sessionId).use { session ->
                session.openWrite("update", 0, apkFile.length()).use { out ->
                    FileInputStream(apkFile).use { input -> input.copyTo(out) }
                    session.fsync(out)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    Intent(ACTION_INSTALL_RESULT).setPackage(context.packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                session.commit(pendingIntent.intentSender)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Install session failed", e)
            runCatching { context.unregisterReceiver(receiver) }
            runCatching { packageInstaller.abandonSession(sessionId) }
            if (cont.isActive) cont.resumeWith(Result.success(false))
        }
    }

    private fun statusName(status: Int): String = when (status) {
        PackageInstaller.STATUS_SUCCESS -> "STATUS_SUCCESS"
        PackageInstaller.STATUS_FAILURE -> "STATUS_FAILURE"
        PackageInstaller.STATUS_FAILURE_ABORTED -> "STATUS_FAILURE_ABORTED"
        PackageInstaller.STATUS_FAILURE_BLOCKED -> "STATUS_FAILURE_BLOCKED"
        PackageInstaller.STATUS_FAILURE_CONFLICT -> "STATUS_FAILURE_CONFLICT"
        PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> "STATUS_FAILURE_INCOMPATIBLE"
        PackageInstaller.STATUS_FAILURE_INVALID -> "STATUS_FAILURE_INVALID"
        PackageInstaller.STATUS_FAILURE_STORAGE -> "STATUS_FAILURE_STORAGE"
        PackageInstaller.STATUS_PENDING_USER_ACTION -> "STATUS_PENDING_USER_ACTION"
        else -> "UNKNOWN($status)"
    }
}
