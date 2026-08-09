package karika.distribucija.ba.salesrep.model

import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import karika.distribucija.ba.salesrep.util.toDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnBehalfOrder(
    @SerialName("order_id") val orderId: Long = 0,
    @SerialName("increment_id") val incrementId: String = "",
    @SerialName("customer_id") val customerId: Long = 0,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("employee_id") val employeeId: Long? = null,
    @SerialName("grand_total") val grandTotal: Float = 0f,
    @SerialName("status") val status: String = "",
    @SerialName("created_at") val createdAt: String? = null
) {
    fun displayName(): String = customerName ?: "Kupac #$customerId"

    fun statusLabel(): String = when (status) {
        "rejected" -> "Odbijeno"
        "approved" -> "Odobreno"
        "cancelled" -> "Otkazano"
        "pending" -> "Na čekanju"
        "processing" -> "U obradi"
        "bill-sent" -> "Uplaćeno"
        "estimate-sent" -> "Čekanje na uplatu"
        else -> status.replaceFirstChar { it.uppercase() }
    }

    fun totalString(): String = karikaPriceFormat(grandTotal.toDouble()) + " KM"

    fun date(): String = createdAt?.toDateTime() ?: ""
}

@Serializable
data class OnBehalfOrderSearchResults(
    @SerialName("items") val items: List<OnBehalfOrder> = emptyList(),
    @SerialName("total_count") val totalCount: Long = 0
)

/** Response from POST /V1/vendor-operations/customers/{customerId}/orders */
@Serializable
data class OnBehalfOrderResult(
    @SerialName("order_id") val orderId: Long = 0,
    @SerialName("increment_id") val incrementId: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("grand_total") val grandTotal: Double = 0.0
)

/** Request body for POST /V1/vendor-operations/customers/{customerId}/orders */
@Serializable
data class OnBehalfPlaceOrderRequest(
    @SerialName("placeRequest") val placeRequest: OnBehalfPlaceRequestMessage
)

@Serializable
data class OnBehalfPlaceRequestMessage(
    @SerialName("message") val message: String
)
