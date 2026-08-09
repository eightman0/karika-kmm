package karika.distribucija.ba.salesrep.model

import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnBehalfProduct(
    @SerialName("entity_id") val entityId: Long = 0,
    @SerialName("sku") val sku: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("price") val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("category_label") val categoryLabel: String? = null
) {
    fun priceString(): String = karikaPriceFormat(price) + " KM"

    fun minQty(): Int = 1
}

@Serializable
data class OnBehalfProductSearchResults(
    @SerialName("items") val items: List<OnBehalfProduct> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0
)
