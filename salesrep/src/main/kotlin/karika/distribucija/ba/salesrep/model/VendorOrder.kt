package karika.distribucija.ba.salesrep.model

import karika.distribucija.ba.salesrep.util.toDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trimmed to the fields SalesOrderDetailView.kt actually renders (full VendorOrder in composeApp
 * carries the vendor's own dashboard order-management fields too, which this sales-rep-only
 * screen never shows).
 */
@Serializable
data class VendorOrder(
    @SerialName("order_id") val orderId: String? = null,
    @SerialName("customer_id") val customerId: String? = null,
    @SerialName("billing_name") val billingName: String? = null,
    @SerialName("order_total") val orderTotal: String? = null,
    @SerialName("shop_commission_fee") val shopCommissionFee: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("real_order_status") val realOrderStatus: String? = null,
    @SerialName("address") val address: ShopAddress? = null,
    @SerialName("products") val products: List<VendorProduct> = emptyList(),
    @SerialName("shipping_details") val shippingDetails: ShippingDetails? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("is_locked") val locked: Boolean? = null
) {
    fun date(): String = createdAt?.toDateTime() ?: "-"

    fun locked() = locked ?: false

    /** Mirrors composeApp's OnBehalfOrder.isPending() (Dashboard.kt) - only a still-pending,
     * unlocked order can have its shipping details or line items edited here. */
    fun isPending(): Boolean = realOrderStatus == "pending"
}

@Serializable
data class ShopAddress(
    @SerialName("street") val street: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("postcode") val postcode: String? = null,
    @SerialName("telephone") val telephone: String? = null
)

/** Mirrors composeApp's domain/model/Dashboard.kt ShippingDetails - the rep-filled shipping
 * address/package form already saved against this order, if any (used to pre-fill the Usluga
 * dostave form when reopening the order detail screen). */
@Serializable
data class ShippingDetails(
    @SerialName("contact_name") val name: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("telephone") val telephone: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("street") val street: String? = null,
    @SerialName("postcode") val postcode: String? = null,
    @SerialName("package_width") val width: String? = null,
    @SerialName("package_depth") val depth: String? = null,
    @SerialName("package_height") val height: String? = null,
    @SerialName("package_weight") val weight: String? = null,
    @SerialName("note") val note: String? = null
)

@Serializable
data class VendorProduct(
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("qty_ordered") val qtyOrdered: String? = null,
    @SerialName("original_qty_ordered") val originalQtyOrdered: String? = null,
    @SerialName("product_unit") val unit: String? = null,
    @SerialName("discount_percent") val discount: String? = "0",
    @SerialName("discount_amount") val discountAmount: String? = "0",
    @SerialName("commission_percent") val commissionPercent: String? = null,
    @SerialName("commission") val commission: String? = null
) {
    fun rabat(): String = discount ?: "0"

    fun qty(): String = "${qtyOrdered ?: "0"} ${unit.orEmpty()}"

    /** Struck-through "was X kom" shown next to the current qty after an edit, matching
     * composeApp's QuantityCell - empty when the order hasn't been edited yet. */
    fun originalQty(): String {
        if (originalQtyOrdered == null || originalQtyOrdered == qtyOrdered) return ""
        return "$originalQtyOrdered ${unit.orEmpty()}"
    }
}

/** Order comment thread. File attachments aren't supported yet - text-only comments for now. */
@Serializable
data class Comment(
    @SerialName("message") val message: String? = "",
    @SerialName("created_at") val createdAt: String? = "",
    @SerialName("is_mine") val isMine: Boolean? = false
) {
    fun isMine() = isMine ?: false
    fun message() = message ?: ""
    fun createdAt() = createdAt ?: ""
}
