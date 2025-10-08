package karika.distribucija.ba.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MandatoryUpdate(
    val ios: String? = null
) {
    fun iOS() = ios?.toDoubleOrNull() ?: 0.0
}