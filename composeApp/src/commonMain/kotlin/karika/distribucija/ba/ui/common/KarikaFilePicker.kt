package karika.distribucija.ba.ui.common

interface KarikaFilePicker {
    fun pickFile(
        mediaTypes: Array<String> = arrayOf("image/png", "image/jpeg"),
        callback: (String, ByteArray) -> Unit
    )

    fun downloadFile(
        fileName: String,
        fileType: String,
        fileUrl: String
    )

    fun getPushHandle(callback: (String, String) -> Unit)
}