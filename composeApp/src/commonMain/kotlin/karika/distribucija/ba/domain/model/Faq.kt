package karika.distribucija.ba.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Faq(
    val question: String?,
    val answer: String?
)