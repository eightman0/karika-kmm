package karika.distribucija.ba

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.arkivanov.decompose.defaultComponentContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import karika.distribucija.ba.ui.common.KarikaFilePicker
import karika.distribucija.ba.util.PushHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity(), KarikaFilePicker {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _: Boolean -> }
    private lateinit var appComponent: AppComponent
    private lateinit var appUpdateManager: AppUpdateManager
    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Ažuriranje otkazano")
                builder.setMessage("Molimo vas da ažurirate aplikaciju kako biste je mogli koristiti.")
                builder.setPositiveButton("Ažuriraj sada") { dialog, _ ->
                    dialog.dismiss()
                    checkUpdate()
                }
                builder.setNegativeButton("Zatvori aplikaciju") { dialog, _ ->
                    finish()
                }
                builder.create().show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        askNotificationPermission()
        appUpdateManager = AppUpdateManagerFactory.create(this)
        super.onCreate(savedInstanceState)

        setContent {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setStatusBarColor(
                    color = Color.Transparent,
                    darkIcons = true // true = dark icons, false = light icons
                )
            }

            if (!::appComponent.isInitialized) {
                appComponent = AppComponent(
                    componentContext = defaultComponentContext(),
                    filePicker = this
                )
            }
            App(appComponent)
            PushHandler.handleNewPushIfExists(intent.extras?.getString("route") ?: "", appComponent)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        registerForActivityResult,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun pickFile(mediaTypes: Array<String>, callback: (String, ByteArray) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                mediaTypes
            )
        }

        val launcher = activityResultRegistry.register(
            "filePicker",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val uri: Uri? = data?.data
                if (uri != null) {
                    var fileName: String? = null
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                    if (fileName == null) {
                        fileName = uri.lastPathSegment ?: "unknown"
                    }

                    val bytes = contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes()
                    }

                    if (bytes != null) {
                        callback(fileName ?: "file", bytes)
                    }
                }
            }
        }
        launcher.launch(intent)
    }

    override fun downloadFile(fileName: String, fileType: String, fileUrl: String) {
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(fileUrl))
        with(request) {
            setTitle(fileName)
            setMimeType("application/pdf")
            setDescription("Preuzimanje pdfa...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        }
        val manager: DownloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    override fun getPushHandle(callback: (String, String) -> Unit) {
        runBlocking(Dispatchers.IO) {
            val fId = FirebaseInstallations.getInstance().id.await()
            val token = FirebaseMessaging.getInstance().token.await()
            callback(fId, token)
        }
    }

    fun checkUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            showImmediateUpdate(appUpdateInfo)
        }
    }

    private fun showImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                registerForActivityResult,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        }
    }
}
