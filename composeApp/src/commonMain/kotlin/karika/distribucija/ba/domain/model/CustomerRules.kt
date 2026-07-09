package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerRuleRequest(
    @SerialName("discountRule")
    val discountRule: DiscountRule
)

@Serializable
data class DiscountRule(
    @SerialName("rule_id")
    val ruleId: Long? = null,
    @SerialName("vendor_id")
    val vendorId: Long? = null,
    @SerialName("discount_type")
    val discountType: String,
    @SerialName("customer_id")
    val customerId: Long? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("customer_group_value")
    val customerGroupValue: String? = null,
    @SerialName("customer_region_value")
    val customerRegionValue: String? = null,
    @SerialName("product_id")
    val productId: Long? = null,
    @SerialName("product_name")
    val productName: String? = null,
    @SerialName("category_id")
    val categoryId: Long? = null,
    @SerialName("category_name")
    val categoryName: String? = null,
    @SerialName("min_qty")
    val minQty: Float? = null,
    @SerialName("discount_percent")
    val discountPercent: Float,
    @SerialName("is_active")
    val isActive: Int,
    @SerialName("starts_at")
    val startsAt: String? = null,
    @SerialName("ends_at")
    val endsAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("created_by_employee_id")
    val createdByEmployeeId: Long? = null,
    /** "approved" | "pending" | "rejected" */
    @SerialName("approval_status")
    val approvalStatus: String? = null
)

@Serializable
data class DiscountRuleSearchResults(
    @SerialName("items")
    val items: List<DiscountRule>
)

@Serializable
data class SearchCriteria(
    @SerialName("filter_groups")
    val filterGroups: List<FilterGroup> = emptyList(),
    @SerialName("sort_orders")
    val sortOrders: List<SortOrder> = emptyList(),
    @SerialName("page_size")
    val pageSize: Int? = null,
    @SerialName("current_page")
    val currentPage: Int? = null
)

@Serializable
data class FilterGroup(
    @SerialName("filters")
    val filters: List<Filter>
)

@Serializable
data class Filter(
    @SerialName("field")
    val field: String,
    @SerialName("value")
    val value: String,
    @SerialName("condition_type")
    val conditionType: String = "eq"
)

@Serializable
data class SortOrder(
    @SerialName("field")
    val field: String,
    @SerialName("direction")
    val direction: String
)
