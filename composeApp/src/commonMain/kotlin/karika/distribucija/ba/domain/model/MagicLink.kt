package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MagicLinkResolveRequest(
    val token: String
)

@Serializable
data class MagicLinkResolveResponse(
    val data: MagicLinkDestination
)

@Serializable
data class MagicLinkDestination(
    val type: String,
    @SerialName("order_id") val orderId: String,
    val audience: List<String> = emptyList()
)

@Serializable
data class MagicLinkErrorResponse(
    val error: MagicLinkError? = null
)

@Serializable
data class MagicLinkError(
    val code: String? = null,
    val message: String? = null
)

sealed class MagicLinkResult {
    data class Success(val destination: MagicLinkDestination) : MagicLinkResult()
    data object Unauthenticated : MagicLinkResult()
    data class Error(val message: String) : MagicLinkResult()
}
