package karika.distribucija.ba.salesrep.model

import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** GET /V1/vendor-operations/customers/{customerId}/cart response. */
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
    @SerialName("shipping_defaults") val shippingDefaults: OnBehalfCartShippingDefaults? = null,
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
    @SerialName("quantity_unit") val quantityUnit: String? = null,
    @SerialName("discount_percent") val discountPercent: Int? = null,
    @SerialName("commission_percent") val commissionPercent: Double = 0.0,
    @SerialName("commission") val commission: Double = 0.0,
    @SerialName("row_total") val rowTotal: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null
) {
    fun priceString(): String = karikaPriceFormat(price) + " KM"
    fun rowTotalString(): String = karikaPriceFormat(rowTotal) + " KM"
}

/**
 * The backend serializes this as a positional JSON array (12 elements, same order as the
 * fields below) rather than the keyed object the API docs describe - this serializer supports
 * both shapes (mirrors composeApp's OnBehalfCartShippingDefaultsSerializer).
 */
@Serializable(with = OnBehalfCartShippingDefaultsSerializer::class)
data class OnBehalfCartShippingDefaults(
    val shippingCompany: String? = null,
    val contactName: String? = null,
    val email: String? = null,
    val telephone: String? = null,
    val city: String? = null,
    val street: String? = null,
    val postcode: String? = null,
    val packageWeight: String? = null,
    val packageWidth: String? = null,
    val packageHeight: String? = null,
    val packageDepth: String? = null,
    val note: String? = null
)

object OnBehalfCartShippingDefaultsSerializer : KSerializer<OnBehalfCartShippingDefaults> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("OnBehalfCartShippingDefaults")

    override fun deserialize(decoder: Decoder): OnBehalfCartShippingDefaults {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("OnBehalfCartShippingDefaults can only be decoded from JSON")

        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> {
                fun field(index: Int): String? {
                    val value = element.getOrNull(index) ?: return null
                    if (value is JsonNull) return null
                    return value.jsonPrimitive.content.takeIf { it.isNotBlank() }
                }
                OnBehalfCartShippingDefaults(
                    shippingCompany = field(0),
                    contactName = field(1),
                    email = field(2),
                    telephone = field(3),
                    city = field(4),
                    street = field(5),
                    postcode = field(6),
                    packageWeight = field(7),
                    packageWidth = field(8),
                    packageHeight = field(9),
                    packageDepth = field(10),
                    note = field(11)
                )
            }

            is JsonObject -> {
                fun field(key: String): String? =
                    (element[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }
                OnBehalfCartShippingDefaults(
                    shippingCompany = field("shipping_company"),
                    contactName = field("contact_name"),
                    email = field("email"),
                    telephone = field("telephone"),
                    city = field("city"),
                    street = field("street"),
                    postcode = field("postcode"),
                    packageWeight = field("package_weight"),
                    packageWidth = field("package_width"),
                    packageHeight = field("package_height"),
                    packageDepth = field("package_depth"),
                    note = field("note")
                )
            }

            else -> OnBehalfCartShippingDefaults()
        }
    }

    override fun serialize(encoder: Encoder, value: OnBehalfCartShippingDefaults) {
        throw SerializationException("OnBehalfCartShippingDefaults is response-only")
    }
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
