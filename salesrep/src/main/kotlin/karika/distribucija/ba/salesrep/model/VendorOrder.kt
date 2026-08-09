package karika.distribucija.ba.salesrep.model

import karika.distribucija.ba.salesrep.util.karikaPriceFormat
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
    @SerialName("products") val products: List<VendorProduct> = emptyList()
) {
    fun date(): String = createdAt?.toDateTime() ?: "-"
}

@Serializable
data class ShopAddress(
    @SerialName("street") val street: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("postcode") val postcode: String? = null,
    @SerialName("telephone") val telephone: String? = null
)

@Serializable
data class VendorProduct(
    @SerialName("item_id") val itemId: String? = null,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("price") val price: String? = null,
    @SerialName("qty_ordered") val qtyOrdered: String? = null,
    @SerialName("product_unit") val unit: String? = null,
    @SerialName("discount_percent") val discount: String? = "0",
    @SerialName("discount_amount") val discountAmount: String? = "0",
    @SerialName("commission_percent") val commissionPercent: String? = null,
    @SerialName("commission") val commission: String? = null
) {
    fun rabat(): String = discount ?: "0"

    fun priceVpc(): String = karikaPriceFormat(price?.toDoubleOrNull() ?: 0.0) + " KM"

    fun totalVpc(): String {
        val unitPrice = price?.toDoubleOrNull() ?: 0.0
        val rabatAmount = discountAmount?.toDoubleOrNull() ?: 0.0
        return karikaPriceFormat((unitPrice * (qtyOrdered?.toIntOrNull() ?: 1)) - rabatAmount) + " KM"
    }

    fun totalWithPdv(): String {
        val unitPrice = price?.toDoubleOrNull() ?: 0.0
        val rabatAmount = discountAmount?.toDoubleOrNull() ?: 0.0
        return karikaPriceFormat(((unitPrice * (qtyOrdered?.toIntOrNull() ?: 1) - rabatAmount) * 1.17)) + " KM"
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
