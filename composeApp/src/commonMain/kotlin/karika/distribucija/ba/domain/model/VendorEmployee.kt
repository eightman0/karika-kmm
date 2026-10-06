package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** Row from `GET /V1/vendor-operations/employees`. */
@Serializable
data class VendorEmployee(
    @SerialName("employee_id")
    val employeeId: Long? = null,

    @SerialName("vendor_id")
    val vendorId: Long? = null,

    @SerialName("customer_id")
    val customerId: Long? = null,

    /** "vendor_admin" | "vendor_manager" | "sales_employee" */
    @SerialName("role")
    val role: String? = null,

    /** "invited" | "active" | "suspended" | "deleted" */
    @SerialName("status")
    val status: String? = null,

    @SerialName("manager_employee_id")
    val managerEmployeeId: Long? = null,

    @SerialName("manager_name")
    val managerName: String? = null,

    @SerialName("full_access")
    val fullAccess: Boolean? = null,

    @SerialName("can_manage")
    val canManage: Boolean? = null,

    /** Gate for location history - show the action only when true and [employeeId] is present. */
    @SerialName("can_view")
    val canView: Boolean? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("firstname")
    val firstname: String? = null,

    @SerialName("lastname")
    val lastname: String? = null,

    @SerialName("telephone")
    val telephone: String? = null,
) {
    val fullName: String
        get() = listOfNotNull(firstname, lastname).joinToString(" ").trim()
            .ifBlank { email.orEmpty() }

    val initials: String
        get() = listOfNotNull(firstname, lastname)
            .mapNotNull { it.trim().firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { email?.firstOrNull()?.uppercase().orEmpty() }

    val canOpenLocations: Boolean
        get() = canView == true && employeeId != null
}

@Serializable
data class VendorEmployeeSearchResults(
    @SerialName("items")
    val items: List<VendorEmployee> = emptyList(),

    @SerialName("total_count")
    val totalCount: Int = 0
)

/** Point from `GET /V1/vendor-operations/employees/{employeeId}/locations`. Timestamps are UTC `Y-m-d H:i:s`. */
@Serializable
data class EmployeeLocationPoint(
    @SerialName("location_id")
    val locationId: Long = 0,

    @SerialName("employee_id")
    val employeeId: Long = 0,

    @SerialName("device_id")
    val deviceId: String = "",

    @SerialName("latitude")
    val latitude: Double = 0.0,

    @SerialName("longitude")
    val longitude: Double = 0.0,

    @SerialName("accuracy")
    val accuracy: Double? = null,

    @SerialName("altitude")
    val altitude: Double? = null,

    /** Meters per second. */
    @SerialName("speed")
    val speed: Double? = null,

    @SerialName("heading")
    val heading: Double? = null,

    @SerialName("device_timestamp")
    val deviceTimestamp: String? = null,

    @SerialName("received_at")
    val receivedAt: String = "",

    @SerialName("effective_at")
    val effectiveAt: String = "",
)

@Serializable
data class EmployeeLocationHistory(
    @SerialName("items")
    val items: List<EmployeeLocationPoint> = emptyList(),

    /** Raw count in the range before sampling. */
    @SerialName("total_count")
    val totalCount: Int = 0,

    /** True when [totalCount] > 1000 and [items] is a thinned sample. */
    @SerialName("sampled")
    val sampled: Boolean = false,

    /** True when the employee has any stored point in any period. */
    @SerialName("has_any")
    val hasAny: Boolean = false,

    @SerialName("last_reported")
    val lastReported: EmployeeLocationPoint? = null,
)

/** Vendor Operations error body - branch on `parameters.code`, `message` is only fallback text. */
@Serializable
data class VendorOperationsError(
    @SerialName("message")
    val message: String? = null,

    /** Object for Vendor Operations errors, but plain Magento errors may send an array here. */
    @SerialName("parameters")
    val parameters: JsonElement? = null,
) {
    val code: String?
        get() = ((parameters as? JsonObject)?.get("code") as? JsonPrimitive)?.content
}
