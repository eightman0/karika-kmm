package karika.distribucija.ba.domain.model

import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.util.karikaPriceFormat
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

@Serializable
data class ProductResponse(
    @SerialName("items") var items: ArrayList<Product> = arrayListOf(),
    @SerialName("total_count") var totalCount: Int? = null
)

@Serializable
data class Product(
    @SerialName("id") var id: Int? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("sku") var sku: String? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("b2b_min_qty") var minQty: String? = null,
    @SerialName("special_price") var specialPrice: Double? = null,
    @SerialName("price") var price: Double? = null,
    @SerialName("status") var status: Int? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("media_gallery_entries") var mediaGalleryEntries: ArrayList<MediaGalleryEntries> = arrayListOf(),
    @SerialName("custom_attributes") var customAttributes: ArrayList<CustomAttributes> = arrayListOf(),
    @SerialName("extension_attributes") var extensionAttributes: ExtensionAttributes? = null,
    @SerialName("rewardPoints") var rewardPoints: Double? = null,
    @SerialName("formatedRewardPoints") var formattedRewardPoints: String? = null,
    @SerialName("news_from_date") var newsFromDate: String? = null,
    @SerialName("news_to_date") var newsToDate: String? = null,
    @SerialName("enable_messaging") var enableMessaging: String? = null,
    @SerialName("stock_data") var stockData: StockData? = null,
    @SerialName("min_quantity_unit") var minQtyUnit: String? = null,
    @SerialName("description") var description: String? = null,
    @SerialName("short_description") var shortDescription: String? = null,
    // V1
    @SerialName("vendor_name") var vendorName: String? = null,
    @SerialName("vendor_id") var vendorId: String? = null,
    @Transient var itemId: Int? = null
) {
    companion object {
        fun of(name: String): Product {
            return Product(
                name = name
            )
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as Product

        if (id != other.id) return false
        if (createdAt != other.createdAt) return false
        if (sku != other.sku) return false
        if (name != other.name) return false
        if (minQty != other.minQty) return false
        if (rewardPoints != other.rewardPoints) return false
        if (price != other.price) return false
        if (status != other.status) return false
        if (image != other.image) return false
        if (mediaGalleryEntries != other.mediaGalleryEntries) return false
        if (customAttributes != other.customAttributes) return false
        if (extensionAttributes != other.extensionAttributes) return false
        if (itemId != other.itemId) return false

        return true
    }

    fun minQtyUnit(): String {
        return (customAttributes.find { it.attributeCode == "min_quantity_unit" }?.value?.jsonPrimitive?.content?.toIntOrNull()
            ?: minQtyUnit?.toIntOrNull() ?: 1).toString()

    }

    fun name() = name ?: "-"
    fun vendorName() = vendorName ?: extensionAttributes?.vendorData?.vendorName ?: "-"
    fun vendorId() = vendorId ?: extensionAttributes?.vendorData?.id ?: "-"
    fun toVendor() = Vendor(
        entityId = vendorId().toIntOrNull() ?: 0,
        publicName = vendorName(),
        companyLogo = "media/" + extensionAttributes?.vendorData?.vendorLogo
    )

    fun specialPrice(): Double {
        return customAttributes
            .find { f -> f.attributeCode == "special_price" }
            ?.value?.jsonPrimitive?.content?.toDouble() ?: specialPrice ?: 0.0
    }

    fun hasSpecialPrice() = specialPriceString() != "0,00 KM"

    fun calculatePercent(): String {
        return "${(((price() - specialPrice()) / price()) * 100).roundToInt()}"
    }

    fun price(): Double {
        return price ?: 0.0
    }

    fun priceString(): String {
        return karikaPriceFormat(price()) + " KM"
    }

    fun specialPriceString(): String {
        return karikaPriceFormat(specialPrice()) + " KM"
    }

    fun bonusString(): String {
        return "${
            karikaPriceFormat(
                (rewardPoints ?: extensionAttributes?.rewardsPoints?.rewardPoints ?: 0.00)
            )
        } KM"
    }

    fun bonusString(qty: Int): String {
        return "${
            karikaPriceFormat(
                (rewardPoints ?: extensionAttributes?.rewardsPoints?.rewardPoints ?: 0.00) * qty
            )
        } KM"
    }

    fun hasBonus() = bonusString() != "0,00 KM"

    fun image(): String {
        if (image != null) {
            return imageUrl(image)
        }

        return imageUrl(customAttributes
            .find { c -> c.attributeCode == "image" }
            ?.value
            ?.jsonPrimitive
            ?.content)
    }

    fun isInStockLabel(): String {
        if (stockData == null) {
            stockData = extensionAttributes?.stockData
        }
        return if (stockData?.isInStock == "1") "Na zalihama" else "Nije na zalihama"
    }

    fun hasOnStock(): Boolean {
        if (stockData == null) {
            stockData = extensionAttributes?.stockData
        }
        return stockData?.isInStock == "1"
    }

    fun minQty(): Int {
        return customAttributes
            .find { f -> f.attributeCode == "b2b_min_qty" }
            ?.value?.jsonPrimitive?.content?.trim()?.toIntOrNull() ?: minQty?.toIntOrNull() ?: 1
    }

    fun breadCrumbs(): String {
        return "Piće > Alkoholna pića > Alkoholna žestoka pića"
    }


    fun vpc(qty: Int): Double {
        val price = if (specialPrice() > 0) specialPrice() else price()
        return price * (qty).toDouble()
    }

    fun vpcString(qty: Int): String {
        return karikaPriceFormat(vpc(qty))
    }

    fun pdv(qty: Int): Double {
        val price = if (specialPrice() > 0) specialPrice() else price()
        return (price * 17 * qty) / 100
    }

    fun pdvString(qty: Int): String {
        return karikaPriceFormat(pdv(qty))
    }

    fun vpcPdvString(qty: Int): String {
        return karikaPriceFormat(vpc(qty) + pdv(qty))
    }

    fun isNew(): Boolean {
        val newsFromDate = newsFromDate ?: (customAttributes
            .find { f -> f.attributeCode == "news_from_date" }
            ?.value?.jsonPrimitive?.content)

        val newsToDate = newsToDate ?: (customAttributes
            .find { f -> f.attributeCode == "news_to_date" }
            ?.value?.jsonPrimitive?.content)

        if (newsFromDate == null || newsToDate == null) {
            return false
        }

        return try {
            val formatter = LocalDateTime.Format {
                year()
                char('-')
                monthNumber()
                char('-')
                dayOfMonth()
                char(' ')
                hour()
                char(':')
                minute()
                char(':')
                second()
            }

            val from = LocalDateTime.parse(newsFromDate, formatter)
                .toInstant(TimeZone.currentSystemDefault())
            val to = LocalDateTime.parse(newsToDate, formatter)
                .toInstant(TimeZone.currentSystemDefault())

            val now = Clock.System.now()

            now in from..to
        } catch (e: Exception) {
            false
        }
    }
}


@Serializable
data class MediaGalleryEntries(
    @SerialName("id") var id: Int? = null,
    @SerialName("media_type") var mediaType: String? = null,
    @SerialName("label") var label: String? = null,
    @SerialName("position") var position: Int? = null,
    @SerialName("disabled") var disabled: Boolean? = null,
    @SerialName("types") var types: ArrayList<String> = arrayListOf(),
    @SerialName("file") var file: String? = null
)

@Serializable
data class ExtensionAttributes(
    @SerialName("vendor_data") var vendorData: VendorData? = null,
    @SerialName("rewards_points") var rewardsPoints: RewardsPoints? = null,
    @SerialName("stock_data") var stockData: StockData? = null
)

@Serializable
data class VendorData(
    @SerialName("vendor_name") var vendorName: String? = "",
    @SerialName("vendor_logo") var vendorLogo: String? = "",
    @SerialName("vendor_id") var id: String? = ""
)

@Serializable
data class RewardsPoints(
    @SerialName("reward_points") var rewardPoints: Double? = 0.00,
    @SerialName("formated_reward_points") var formattedRewardPoints: String? = ""
)

@Serializable
data class StockData(
    @SerialName("stock_salable_qty") var salableQty: Long? = 0,
    @SerialName("is_in_stock") var isInStock: String? = "0"
)