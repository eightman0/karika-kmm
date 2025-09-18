package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    @SerialName("quantity_unit_options") var unitOptions: List<KarikaUnit> = listOf(),
    @SerialName("customer_region_list") var customerRegionList: List<KarikaUnit> = listOf(),
    @SerialName("customer_group_list") var customerGroupList: List<KarikaUnit> = listOf(),
    @SerialName("shipping_proveders") var shippingProveders: List<DeliveryProvider> = listOf(),
) {
    fun a2b() = shippingProveders.find { it.code == "A2B" }?.price
    fun express() = shippingProveders.find { it.code == "EURO_EXPRESS" }?.price
}

@Serializable
data class DeliveryProvider(
    @SerialName("code") var code: String? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("shipping_price_increase_percentage") var percent: String? = null,
    @SerialName("price_list") var price: List<DeliveryPrice> = emptyList()
)

@Serializable
data class DeliveryPrice(
    @SerialName("min") var min: Int? = null,
    @SerialName("max") var max: Int? = null,
    @SerialName("price") var price: Double? = null
) {
    fun min() = min ?: 0
    fun max() = max ?: 0
    fun price() = price ?: 0.0
}

@Serializable
data class KarikaUnit(
    @SerialName("label") var label: String? = null,
    @SerialName("value") var unit: String? = null
) {
    fun label() = label ?: ""
    fun unit() = unit?.replace("|", "")?.trim() ?: ""
}