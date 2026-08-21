package karika.distribucija.ba.salesrep.update

data class KioskVersion(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val apkSha256: String,
    val mandatory: Boolean
) {
    val isPublished: Boolean get() = versionCode > 0 && apkUrl.isNotBlank()
}
