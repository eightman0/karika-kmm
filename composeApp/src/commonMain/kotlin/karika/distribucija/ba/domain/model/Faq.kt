package karika.distribucija.ba.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Faq(
    val section: String?,
    val items: List<Answer>?
)

@Serializable
data class Answer(
    val question: String?,
    val answer: String?
)