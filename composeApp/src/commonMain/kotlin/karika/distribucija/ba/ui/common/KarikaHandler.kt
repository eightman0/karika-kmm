package karika.distribucija.ba.ui.common

interface KarikaHandler {
    fun pickFile(
        mediaTypes: Array<String> = arrayOf("application/pdf"),
        callback: (String, ByteArray) -> Unit
    )

    fun pickPhoto(callback: (String, ByteArray) -> Unit)

    fun takePhoto(callback: (String, ByteArray) -> Unit) {}

    fun downloadFile(
        fileName: String,
        fileType: String,
        fileUrl: String
    )

    fun getPushHandle(callback: (String, String) -> Unit)

    fun exitKiosk() {}

    fun checkForUpdate() {}

    fun openWifi() {}
}