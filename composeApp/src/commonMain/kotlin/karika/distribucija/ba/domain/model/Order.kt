package karika.distribucija.ba.domain.model

import androidx.compose.ui.graphics.Color
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.toDateString
import karika.distribucija.ba.util.karikaPriceFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

@Serializable
data class OrdersResponse(
    @SerialName("total_due") var total: Double = 0.0,
    @SerialName("entity_id") var orderId: String = "",
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("mp_reward_earn") var bonus: Double? = null,
    @SerialName("increment_id") var incrementId: String? = null,
    @SerialName("vendor_orders") var orders: ArrayList<Order> = arrayListOf(),
    @Transient var status: String? = null
) {
    fun vpcPdvString(): String {
        return Product().apply {
            price = total
        }.vpcPdvString(1) + " KM"
    }

    fun pdvString(): String {
        return Product().apply {
            price = total
        }.pdvString(1) + " KM"
    }

    fun vpcString(): String {
        return Product().apply {
            price = total
        }.vpcString(1) + " KM"
    }

    fun bonus(): String {
        return karikaPriceFormat(bonus ?: 0.00) + " KM"
    }

    fun date() = createdAt?.toDateString() ?: ""
}

@Serializable
data class Order(
    @SerialName("order_total") var total: Double,
    @SerialName("product_qty") var qty: Double,
    @SerialName("real_order_status") var status: String,
    @SerialName("vendorName") var vendorName: String? = null,
    @SerialName("reject_reason") var rejectReason: String? = null,
    @SerialName("comments_count") var commentCount: Int = 0,
    @SerialName("vendor_id") var vendorId: Int? = null,
    @SerialName("order_id") var orderId: String? = null,
    @SerialName("products") var products: ArrayList<OrderProduct> = arrayListOf(),
    @SerialName("has_changes") var hasChange: String = "0",
) {
    fun vpcString() = karikaPriceFormat(total) + " KM"
    fun vpcPdvString() = karikaPriceFormat(total * 1.17) + " KM"
    fun status() = when (status) {
        "rejected" -> "Odbijena"
        "approved" -> "Odobrena"
        "cancelled" -> "Otkazana"
        "bill-sent" -> "Uplaćena"
        "estimate-sent" -> "Čekanje na uplatu"
        "pending" -> "Na čekanju"
        else -> "Nepoznato"
    }

    fun statusColor(): Color {
        return when (status) {
            "rejected" -> KarikaColors.Red.copy(alpha = 0.1f)
            "approved" -> KarikaColors.Green3.copy(alpha = 0.1f)
            "cancelled" -> KarikaColors.Gray2.copy(alpha = 0.1f)
            "bill-sent" -> KarikaColors.Orange.copy(alpha = 0.1f)
            "estimate-sent" -> KarikaColors.Orange.copy(alpha = 0.1f)
            "pending" -> KarikaColors.Blue.copy(alpha = 0.1f)
            else -> KarikaColors.Pending
        }
    }

    fun statusTextColor(): Color {
        return when (status) {
            "rejected" -> KarikaColors.Red
            "approved" -> KarikaColors.Green3
            "cancelled" -> KarikaColors.Gray2
            "bill-sent" -> KarikaColors.Orange
            "estimate-sent" -> KarikaColors.Orange
            "pending" -> KarikaColors.Blue
            else -> KarikaColors.Orange
        }
    }

    fun canceled() = listOf("cancelled", "rejected", "approved").contains(status)

    fun showAddBill() = status == "estimate-sent"
}

@Serializable
data class OrderProduct(
    @SerialName("product_id") var id: Int,
    @SerialName("name") var name: String? = null,
    @SerialName("price") var discountedPrice: Double? = null,
    @SerialName("original_price") var price: Double? = null,
    @SerialName("row_total") var total: Double? = null,
    @SerialName("mp_reward_earn") var bonus: Double? = null,
    @SerialName("sku") var sku: String? = null,
    @SerialName("qty_ordered") var qty: Double? = null,
    @SerialName("original_qty_ordered") var originalQty: Double? = null,
    @SerialName("discount_percent") var discountPercent: Double? = null,
    @SerialName("original_discount_amount") var originalDiscountAmount: Double? = null,
    @SerialName("product_options") var productOptions: ProductOptions? = null,
    @Transient var vendorName: String? = null
) {
    fun qtyUnit(): String {
        return productOptions?.additionalOptions?.find { it.code == "quantity_unit" }?.value
            ?: "kom"
    }

    fun qty(): String {
        return "${qty?.roundToInt() ?: 0} ${qtyUnit()}"
    }

    fun originalQty(): String {
        return "${(originalQty ?: 0.0).roundToInt()} ${qtyUnit()}"
    }

    fun qtyChanged() = qty != originalQty

    fun rabat() = discountPercent?.roundToInt()?.toString() ?: "0"

    fun vpc(): String {
        return karikaPriceFormat(
            Product().apply {
                price = this@OrderProduct.price
            }.vpc(1)
        ) + " KM"
    }

    fun total(): String {
        return karikaPriceFormat(
            Product().apply {
                price = total
            }.vpc(qty?.toInt() ?: 1)
        ) + " KM"
    }

    fun vendorName() = productOptions?.additionalOptions?.find { it.code == "vendor_name" }?.value
}

@Serializable
data class Bonus(
    @SerialName("reward_id") var rewardId: Int = 0,
    @SerialName("customer_id") var customerId: Int = 0,
    @SerialName("point_balance") var pointBalance: Double = 0.0,
    @SerialName("point_spent") var pointSpent: Double = 0.0,
    @SerialName("is_active") var isActive: Boolean = false
)

@Serializable
data class Transaction(
    @SerialName("transaction_id") var transactionId: String,
    @SerialName("reward_id") var rewardId: String? = null,
    @SerialName("customer_id") var customerId: String? = null,
    @SerialName("action_code") var actionCode: String? = null,
    @SerialName("action_type") var actionType: String? = null,
    @SerialName("store_id") var storeId: String? = null,
    @SerialName("point_amount") var pointAmount: String,
    @SerialName("point_remaining") var pointRemaining: String? = null,
    @SerialName("point_used") var pointUsed: String? = null,
    @SerialName("status") var status: String,
    @SerialName("order_id") var orderId: String? = null,
    @SerialName("created_at") var createdAt: String,
    @SerialName("expiration_date") var expirationDate: String? = null,
    @SerialName("expire_email_sent") var expireEmailSent: String? = null,
    @SerialName("extra_content") var extraContent: String
) {
    fun id(): String {
        return Json.decodeFromString<JsonObject>(extraContent)["increment_id"]
            ?.jsonPrimitive
            ?.content ?: ""
    }

    fun bonus() = karikaPriceFormat(pointAmount.toDoubleOrNull() ?: 0.0) + " KM"
}

@Serializable
data class Comment(
    @SerialName("message") var message: String? = "",
    @SerialName("created_at") var createdAt: String? = "",
    @SerialName("is_mine") var isMine: Boolean? = false,
    @SerialName("files") var files: List<File>? = emptyList(),
) {
    fun isMine() = isMine ?: false
    fun message() = message ?: ""
    fun createdAt() = createdAt ?: ""
}

@Serializable
data class File(
    @SerialName("name") var name: String? = "",
    @SerialName("url") var url: String? = "",
    @SerialName("type") var type: String? = ""
)

@Serializable
data class Tracking(
    @SerialName("package_status") var status: String? = "",
    @SerialName("location") var location: String? = "",
    @SerialName("modified_on") var modifiedOn: String? = "",
    @SerialName("last_status") var lastStatus: Boolean? = false,
    @SerialName("note") var note: String? = "",
)

@Serializable
data class ProductOptions(
    @SerialName("additional_options") var additionalOptions: List<AdditionalOption>? = emptyList()
)

@Serializable
data class AdditionalOption(
    @SerialName("code") var code: String? = null,
    @SerialName("label") var label: String? = null,
    @SerialName("value") var value: String? = null
)