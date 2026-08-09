package karika.distribucija.ba.salesrep.model

import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GET /V1/vendor-operations/customers/{customerId}/cart response, trimmed to what
 * catalog/cart need - shippingDefaults (used only by order review/checkout) is a follow-up. */
@Serializable
data class OnBehalfCartResponse(
    @SerialName("quote_id") val quoteId: Long = 0,
    @SerialName("customer_id") val customerId: Long = 0,
    @SerialName("items_count") val itemsCount: Int = 0,
    @SerialName("grand_total") val grandTotal: Double = 0.0,
    @SerialName("subtotal") val subtotal: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("total_tax") val totalTax: Double = 0.0,
    @SerialName("total_with_tax") val totalWithTax: Double = 0.0,
    @SerialName("fee") val fee: Double = 0.0,
    @SerialName("items") val items: List<OnBehalfCartResponseItem> = emptyList()
) {
    val isEmpty: Boolean get() = items.isEmpty()

    fun subtotalString(): String = karikaPriceFormat(subtotal) + " KM"
    fun discountString(): String = karikaPriceFormat(discountAmount) + " KM"
    fun grandTotalString(): String = karikaPriceFormat(grandTotal) + " KM"
}

@Serializable
data class OnBehalfCartResponseItem(
    @SerialName("item_id") val itemId: Long = 0,
    @SerialName("sku") val sku: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("price") val price: Double = 0.0,
    @SerialName("qty") val qty: Int = 0,
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("commission_percent") val commissionPercent: Double = 0.0,
    @SerialName("commission") val commission: Double = 0.0,
    @SerialName("row_total") val rowTotal: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null
) {
    fun priceString(): String = karikaPriceFormat(price) + " KM"
    fun rowTotalString(): String = karikaPriceFormat(rowTotal) + " KM"
}

/** Request body for POST /V1/vendor-operations/customers/{customerId}/cart/items */
@Serializable
data class OnBehalfCartItemRequest(
    @SerialName("cartItem") val cartItem: OnBehalfCartItemInput
)

@Serializable
data class OnBehalfCartItemInput(
    @SerialName("sku") val sku: String,
    @SerialName("qty") val qty: Int,
    @SerialName("discount_percent") val discountPercent: Int? = null
)
