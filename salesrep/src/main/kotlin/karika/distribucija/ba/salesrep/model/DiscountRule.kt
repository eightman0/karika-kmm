package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscountRuleInput(
    @SerialName("discount_type") val discountType: String = "per_customer",
    @SerialName("discount_percent") val discountPercent: Float,
    @SerialName("is_active") val isActive: Int = 1,
    @SerialName("product_id") val productId: Long? = null,
    @SerialName("category_id") val categoryId: Long? = null,
    @SerialName("min_qty") val minQty: Float? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null
)

@Serializable
data class DiscountRuleBody(
    @SerialName("discountRule") val discountRule: DiscountRuleInput
)

@Serializable
data class DiscountRule(
    @SerialName("rule_id") val ruleId: Long? = null,
    @SerialName("vendor_id") val vendorId: Long? = null,
    @SerialName("discount_type") val discountType: String,
    @SerialName("customer_id") val customerId: Long? = null,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("product_id") val productId: Long? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("category_id") val categoryId: Long? = null,
    @SerialName("category_name") val categoryName: String? = null,
    @SerialName("min_qty") val minQty: Float? = null,
    @SerialName("discount_percent") val discountPercent: Float,
    @SerialName("is_active") val isActive: Int,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("created_by_employee_id") val createdByEmployeeId: Long? = null,
    /** "approved" | "pending" | "rejected" */
    @SerialName("approval_status") val approvalStatus: String? = null
) {
    fun targetLabel(): String = when {
        productId != null -> productName ?: "Artikal #$productId"
        categoryId != null -> categoryName ?: "Kategorija #$categoryId"
        else -> "Svi artikli i kategorije"
    }

    /** Uppercase to match DiscountCard's badge text in SalesCustomerDetailView.kt. */
    fun approvalLabel(): String = when (approvalStatus) {
        "approved" -> "ODOBRENO"
        "pending" -> "NA ČEKANJU"
        "rejected" -> "ODBIJENO"
        else -> approvalStatus?.uppercase() ?: "—"
    }
}

@Serializable
data class DiscountRuleSearchResults(
    @SerialName("items") val items: List<DiscountRule> = emptyList()
)
