package karika.distribucija.ba.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class PartnershipRequest(
    @SerialName("partnership_id")
    val partnershipId: Long,
    @SerialName("vendor_id")
    val vendorId: Long? = null,
    @SerialName("customer_id")
    val customerId: Long? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("requested_by")
    val requestedBy: String? = null,
    @SerialName("note")
    val note: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("customer_email")
    val customerEmail: String? = null,
    @SerialName("customer_firstname")
    val customerFirstname: String? = null,
    @SerialName("customer_lastname")
    val customerLastname: String? = null,
    @SerialName("customer_company")
    val customerCompany: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
) {
    fun displayVendorName(): String = vendorName?.takeIf { it.isNotBlank() } ?: "—"

    fun requestedAt(): String? = createdAt?.toPartnershipDateTime()
}

@Serializable
data class PartnershipRequestsResponse(
    @SerialName("items")
    val items: List<PartnershipRequest> = emptyList(),
    @SerialName("total_count")
    val totalCount: Int? = null,
)

@Serializable
data class RejectPartnershipRequest(
    @SerialName("action")
    val action: RejectPartnershipAction,
)

@Serializable
data class RejectPartnershipAction(
    @SerialName("reason")
    val reason: String,
)

@Serializable
data class PartnershipErrorResponse(
    @SerialName("message")
    val message: String? = null,
    @SerialName("parameters")
    val parameters: PartnershipErrorParameters? = null,
)

@Serializable
data class PartnershipErrorParameters(
    @SerialName("code")
    val code: String? = null,
)

fun partnershipErrorMessage(code: String?): String = when (code) {
    "forbidden_partnership_action" -> "Nemate dozvolu za ovu akciju."
    "partnership_not_for_customer" -> "Ovaj zahtjev za partnerstvo nije namijenjen Vama."
    "invalid_partnership_transition" -> "Zahtjev je već obrađen."
    "partnership_not_found" -> "Zahtjev za partnerstvo nije pronađen."
    else -> "Došlo je do greške. Pokušajte ponovo!"
}

@OptIn(ExperimentalTime::class)
private fun String.toPartnershipDateTime(): String {
    return try {
        val localDateTime = Instant.parse(this).toLocalDateTime(TimeZone.of("Europe/Sarajevo"))
        val dateFormat = LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
            char('.')
            char(' ')
            hour()
            char(':')
            minute()
        }
        localDateTime.format(dateFormat)
    } catch (e: Exception) {
        this
    }
}

@Serializable
data class Partnership(
    @SerialName("partnership_id") val partnershipId: Long? = null,
    @SerialName("vendor_id") val vendorId: Long? = null,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("requested_by") val requestedBy: String? = null,
    @SerialName("created_by_employee_id") val createdByEmployeeId: Long? = null
)

@Serializable
data class PartnershipRequestBody(
    @SerialName("request") val request: PartnershipRequest
)
