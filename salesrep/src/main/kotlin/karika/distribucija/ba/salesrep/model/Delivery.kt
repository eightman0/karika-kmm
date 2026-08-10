package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors composeApp's domain/model/Config.kt, trimmed to the shipping-provider data the
 * Order Review screen's delivery-price calculator needs. */
@Serializable
data class Config(
    @SerialName("shipping_proveders") val shippingProveders: List<DeliveryProvider> = emptyList()
)

@Serializable
data class DeliveryProvider(
    @SerialName("code") val code: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("shipping_price_increase_percentage") val percent: String? = null,
    @SerialName("price_list") val price: List<DeliveryPrice> = emptyList()
) {
    /** Band lookup + provider markup for a given chargeable weight/volume. */
    fun priceFor(chargeable: Double): Double {
        val band = price.find { it.min() <= chargeable && it.max() >= chargeable } ?: price.lastOrNull()
        val base = band?.price() ?: 0.0
        val pct = percent?.toDoubleOrNull() ?: 0.0
        return base * (1 + pct / 100)
    }
}

@Serializable
data class DeliveryPrice(
    @SerialName("min") val min: Int? = null,
    @SerialName("max") val max: Int? = null,
    @SerialName("price") val price: Double? = null
) {
    fun min() = min ?: 0
    fun max() = max ?: 0
    fun price() = price ?: 0.0
}

/** Mirrors composeApp's domain/model/Dashboard.kt VendorDeliveryServiceData - the shipping
 * address/package form submitted to mobile/vendor/order/shippingDetails after an order is
 * placed, if the rep filled it in. [id] is the order's incrementId, set only right before
 * sending (see OrderReviewViewModel.confirmOrder()). */
@Serializable
data class VendorDeliveryServiceData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val telephone: String = "",
    val city: String = "",
    val street: String = "",
    val postcode: String = "",
    val weight: String = "",
    val width: String = "",
    val height: String = "",
    val depth: String = "",
    val note: String = "",
    val companyCode: String = ""
)
