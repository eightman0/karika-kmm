package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Partnership(
    @SerialName("partnership_id") val partnershipId: Long? = null,
    @SerialName("vendor_id") val vendorId: Long? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("status") val status: String? = null
)

@Serializable
data class PartnershipRequestBody(
    @SerialName("request") val request: PartnershipRequest
)

@Serializable
data class PartnershipRequest(
    @SerialName("customer_email") val customerEmail: String,
    @SerialName("note") val note: String? = null
)
