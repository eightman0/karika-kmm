package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Magento error envelope - Vendor Operations errors carry a stable `parameters.code`. */
@Serializable
data class MagentoError(
    @SerialName("message") val message: String? = null,
    @SerialName("parameters") val parameters: JsonObject? = null
) {
    val code: String? get() = (parameters?.get("code") as? JsonPrimitive)?.contentOrNull

    companion object {
        private val json = Json { ignoreUnknownKeys = true; isLenient = true }

        fun parse(body: String?): MagentoError? =
            body?.takeIf { it.isNotBlank() }?.let { runCatching { json.decodeFromString<MagentoError>(it) }.getOrNull() }
    }
}

/** One order line to repeat into the on-behalf cart ("Naruči ponovo"). */
data class ReorderLine(val sku: String, val name: String, val qty: Int)

/** Result of repeating an order into the on-behalf cart. [cart] is the latest server cart seen. */
sealed class ReorderOutcome {
    /** At least one line was added - open the cart review. */
    data class Added(val cart: OnBehalfCartResponse?, val skippedNames: List<String>) : ReorderOutcome()

    /** Every line was skipped - stay on the order screen. */
    data class AllSkipped(val cart: OnBehalfCartResponse?, val skippedNames: List<String>) : ReorderOutcome()

    /** The order had no lines at all. */
    data object NoLines : ReorderOutcome()

    /** Cart GET/DELETE failed, partnership inactive, forbidden, or the network failed. */
    data class Failed(val code: String?, val message: String?, val status: Int? = null) : ReorderOutcome()
}
