package karika.distribucija.ba.domain.model

import karika.distribucija.ba.ui.common.appUrl
import karika.distribucija.ba.ui.common.isAndroid
import kotlinx.serialization.Serializable

@Serializable
data class MandatoryUpdate(
    val ios: String? = null,
    val android: String? = null,
    val androidUrl: String? = null,
    val iosUrl: String? = null,
) {
    fun version() = (if (isAndroid()) android?.replace(".", "") else ios?.replace(".", ""))
        ?.toIntOrNull() ?: 0

    fun androidUpdateUrl() = androidUrl ?: appUrl()
    fun iosUpdateUrl() = iosUrl ?: appUrl()

    fun updateUrl() = if (isAndroid()) androidUpdateUrl() else iosUpdateUrl()
}