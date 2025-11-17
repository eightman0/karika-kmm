package karika.distribucija.ba.domain.model

import karika.distribucija.ba.ui.common.isAndroid
import kotlinx.serialization.Serializable

@Serializable
data class MandatoryUpdate(
    val ios: String? = null,
    val android: String? = null,
) {
    fun version() = (if (isAndroid()) android?.replace(".", "") else ios?.replace(".", ""))
        ?.toIntOrNull() ?: 0
}