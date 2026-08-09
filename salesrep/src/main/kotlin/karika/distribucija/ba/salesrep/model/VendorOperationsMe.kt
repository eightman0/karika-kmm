package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VendorOperationsMe(
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("vendor_id") val vendorId: Long? = null,
    @SerialName("employee_id") val employeeId: Long? = null,
    @SerialName("manager_employee_id") val managerEmployeeId: Long? = null,
    @SerialName("name") val name: String? = null,
    /** ApiRole: "customer" | "vendor_admin" | "vendor_manager" | "sales_employee" */
    @SerialName("role") val role: String? = null,
    @SerialName("is_vendor_owner") val isVendorOwner: Boolean = false,
    @SerialName("vendor_operations_enabled") val vendorOperationsEnabled: Boolean = false,
    @SerialName("is_full_access") val isFullAccess: Boolean = false,
    @SerialName("capabilities") val capabilities: MeCapabilities = MeCapabilities()
)

@Serializable
data class MeCapabilities(
    @SerialName("can_manage_employees") val canManageEmployees: Boolean = false,
    @SerialName("can_view_employees") val canViewEmployees: Boolean = false,
    @SerialName("can_configure_vendor") val canConfigureVendor: Boolean = false,
    @SerialName("can_request_partnership") val canRequestPartnership: Boolean = false,
    @SerialName("can_approve_partnership") val canApprovePartnership: Boolean = false,
    @SerialName("can_revoke_partnership") val canRevokePartnership: Boolean = false,
    @SerialName("can_assign_customers") val canAssignCustomers: Boolean = false,
    @SerialName("can_create_customer") val canCreateCustomer: Boolean = false,
    @SerialName("can_see_all_vendor_customers") val canSeeAllVendorCustomers: Boolean = false,
    @SerialName("can_see_customer") val canSeeCustomer: Boolean = false,
    @SerialName("can_create_discount_for") val canCreateDiscountFor: Boolean = false,
    @SerialName("can_approve_discount") val canApproveDiscount: Boolean = false,
    @SerialName("can_approve_order") val canApproveOrder: Boolean = false,
    @SerialName("can_place_order_for") val canPlaceOrderFor: Boolean = false,
    @SerialName("can_see_all_activity") val canSeeAllActivity: Boolean = false,
    @SerialName("can_see_dashboard") val canSeeDashboard: Boolean = false,
    @SerialName("is_scoped_manager") val isScopedManager: Boolean = false,
    @SerialName("can_use_messages") val canUseMessages: Boolean = false,
    @SerialName("can_message_staff") val canMessageStaff: Boolean = false
)
