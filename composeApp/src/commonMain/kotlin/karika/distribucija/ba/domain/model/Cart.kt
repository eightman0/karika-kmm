package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Cart(
    @SerialName("id") var id: Int? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("updated_at") var updatedAt: String? = null,
    @SerialName("items") var items: ArrayList<CartItem> = arrayListOf(),
    @SerialName("items_count") var itemsCount: Int? = null,
    @SerialName("items_qty") var itemsQty: Int? = null,
    @SerialName("extension_attributes") var extensionAttributes: ExtAttributes? = null,
)

@Serializable
data class SetShippingAddressRequest(
    val addressInformation: ShippingAddress
)

@Serializable
data class ShippingAddress(
    val shippingAddress: Address,
    val billingAddress: Address,
    @SerialName("shipping_carrier_code") val shippingCode: String,
    @SerialName("shipping_method_code") val shippingMethodCode: String
)

@Serializable
data class PlaceOrder(
    val paymentMethod: PaymentMethod
)

@Serializable
data class PaymentMethod(
    val method: String
)

@Serializable
data class AddToCart(
    val cartItem: CartItem
)

@Serializable
data class CartItem(
    @SerialName("item_id") val itemId: Int? = null,
    @SerialName("sku") val sku: String,
    @SerialName("qty") val qty: Int,
    @SerialName("name") val name: String? = null,
    @SerialName("price") val price: Double? = null,
    @SerialName("product_type") val productType: String? = null,
    @SerialName("quote_id") val quoteId: String,
    @SerialName("extension_attributes") var extensionAttributes: ExtAttributes? = null
) {
    val productId = extensionAttributes?.productId ?: ""
}

@Serializable
data class ExtAttributes(
    @SerialName("product_id")
    val productId: String? = null,
    @SerialName("vendor_id")
    val vendorId: String? = null,
    @SerialName("product_unit")
    val productUnit: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("special_price")
    val specialPrice: String? = null,
    @SerialName("vendors")
    val vendors: List<Vendor>? = null,
    @SerialName("reward_points")
    val rewardPoints: Double? = null,
    @SerialName("min_qty")
    val minQty: Int? = null,
)

@Serializable
data class CartVendor(
    @SerialName("id") val id: String? = null,
    @SerialName("min_order_amount") val minOrderAmount: String? = null
) {
    fun amount(): Double {
        return minOrderAmount?.toDoubleOrNull() ?: 0.0
    }
}