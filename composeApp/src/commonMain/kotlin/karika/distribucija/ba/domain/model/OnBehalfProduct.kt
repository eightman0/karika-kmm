package karika.distribucija.ba.domain.model

import karika.distribucija.ba.util.karikaPriceFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnBehalfProduct(
    @SerialName("entity_id") val entityId: Long = 0,
    @SerialName("sku") val sku: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("price") val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category_ids") val categoryIds: List<Long> = emptyList(),
    @SerialName("category_label") val categoryLabel: String? = null,
    @SerialName("is_in_stock") val isInStock: Boolean = true,
    @SerialName("salable_qty") val salableQty: Double? = null
) {
    /** Cart map key */
    val key: String get() = entityId.toString()

    /** VPC total for given quantity (price is per unit) */
    fun vpc(qty: Int): Double = price * qty

    /** PDV 17% on VPC */
    fun pdv(qty: Int): Double = vpc(qty) * 0.17

    fun priceString(): String = karikaPriceFormat(price) + " KM"

    fun minQty(): Int = 1
}

@Serializable
data class OnBehalfProductSearchResults(
    @SerialName("items") val items: List<OnBehalfProduct> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0
)
