package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssignedEmployeeSummary(
    @SerialName("employee_id") val employeeId: Long,
    @SerialName("display_name") val displayName: String? = null
)

@Serializable
data class OperationalCustomer(
    @SerialName("customer_id") val customerId: Long,
    @SerialName("email") val email: String? = null,
    @SerialName("firstname") val firstname: String? = null,
    @SerialName("lastname") val lastname: String? = null,
    @SerialName("company") val company: String? = null,
    @SerialName("partnership_id") val partnershipId: Long,
    /** "pending" | "active" | "rejected" | "revoked" */
    @SerialName("partnership_status") val partnershipStatus: String,
    @SerialName("default_billing_address_id") val defaultBillingAddressId: Long? = null,
    @SerialName("default_shipping_address_id") val defaultShippingAddressId: Long? = null,
    @SerialName("assigned_employees") val assignedEmployees: List<AssignedEmployeeSummary> = emptyList()
) {
    val isActive: Boolean get() = partnershipStatus == "active"
    val fullName: String get() = listOfNotNull(firstname, lastname).joinToString(" ").ifEmpty { "—" }

    fun statusLabel(): String = when (partnershipStatus) {
        "active" -> "Aktivan"
        "pending" -> "Na čekanju"
        "rejected" -> "Odbijen"
        "revoked" -> "Ukinut"
        else -> partnershipStatus.replaceFirstChar { it.uppercase() }
    }

    /** Matches SalesCustomersView.kt's CustomerCard badgeLabel (uppercase, distinct from statusLabel()). */
    fun badgeLabel(): String = when (partnershipStatus) {
        "active" -> "AKTIVAN"
        "pending" -> "NA ČEKANJU"
        "revoked" -> "OPOZVAN"
        "rejected" -> "ODBIJEN"
        else -> partnershipStatus.uppercase()
    }

    /** Matches SalesCustomerDetailView.kt's profile badge wording - a third, distinct variant. */
    fun detailBadgeLabel(): String = when (partnershipStatus) {
        "active" -> "AKTIVNO"
        "pending" -> "NA ČEKANJU"
        "rejected" -> "ODBIJENO"
        "revoked" -> "OPOZVANO"
        else -> partnershipStatus.uppercase()
    }
}

@Serializable
data class OperationalCustomerSearchResults(
    @SerialName("items") val items: List<OperationalCustomer> = emptyList(),
    @SerialName("total_count") val totalCount: Long = 0
)
