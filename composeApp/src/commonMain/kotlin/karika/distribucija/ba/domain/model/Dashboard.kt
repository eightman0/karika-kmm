package karika.distribucija.ba.domain.model

import androidx.compose.ui.graphics.Color
import coil3.Uri
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
import karika.distribucija.ba.util.karikaPriceFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class DashboardData(
    @SerialName("pending_amount_total") var pendingAmountTotal: String? = null,
    @SerialName("earned_amount_total") var earnedAmountTotal: String? = null,
    @SerialName("orders_placed_total") var ordersPlacedTotal: String? = null,
    @SerialName("products_sold_total") var productsSoldTotal: String? = null,
    @SerialName("approved_orders_total") var approvedOrdersTotal: String? = null,
    @SerialName("approved_orders_count") var approvedOrdersCount: String? = null,
    @SerialName("pending_products") var pendingProducts: String? = null,
    @SerialName("approved_products") var approvedProducts: String? = null,
    @SerialName("disapproved_products") var disapprovedProducts: String? = null,
    @SerialName("latest_products") var latestProducts: List<LatestProducts>? = listOf(),
    @SerialName("best_seller_products") var bestSellerProducts: List<BestSellerProducts>? = listOf(),
    @SerialName("latest_orders") var latestOrders: List<LatestOrders>? = listOf()
) {
    fun approved() = (approvedOrdersCount?.toFloatOrNull() ?: 0f) / total()
    fun pending() =
        ((ordersPlacedTotal?.toFloatOrNull() ?: 0f) - (approvedOrdersCount?.toFloatOrNull()
            ?: 0f)) / total()

    fun total() = ordersPlacedTotal?.toFloatOrNull() ?: 0f

    fun approvedTotal() = karikaPriceFormat(approvedOrdersTotal?.toDoubleOrNull() ?: 0.0)
}

@Serializable
data class LatestProducts(
    @SerialName("product_id") var productId: String? = null,
    @SerialName("product_name") var productName: String? = null,
    @SerialName("product_price") var productPrice: String? = null,
    @SerialName("quantity") var quantity: String? = null,
    @SerialName("status") var status: String? = null
)

@Serializable
data class BestSellerProducts(
    @SerialName("product_id") var productId: String? = null,
    @SerialName("product_name") var productName: String? = null,
    @SerialName("product_price") var productPrice: String? = null,
    @SerialName("ordered_quantity") var orderedQuantity: String? = null
) {
    fun price() = karikaPriceFormat(productPrice?.toDoubleOrNull() ?: 0.0)
}

@Serializable
data class LatestOrders(
    @SerialName("order_id") var orderId: String? = null,
    @SerialName("order_total") var orderTotal: String? = null,
    @SerialName("billing_name") var billingName: String? = null,
    @SerialName("pravno_lice") var pravnoLice: String? = null,
    @SerialName("real_order_status") var realOrderStatus: String? = null
)


@Serializable
data class VendorOrder(
    @SerialName("id") var id: String? = null,
    @SerialName("vendor_id") var vendorId: String? = null,
    @SerialName("order_id") var orderId: String? = null,
    @SerialName("currency") var currency: String? = null,
    @SerialName("base_order_total") var baseOrderTotal: String? = null,
    @SerialName("order_total") var orderTotal: String? = null,
    @SerialName("shop_commission_type_id") var shopCommissionTypeId: String? = null,
    @SerialName("shop_commission_rate") var shopCommissionRate: String? = null,
    @SerialName("shop_commission_base_fee") var shopCommissionBaseFee: String? = null,
    @SerialName("shop_commission_fee") var shopCommissionFee: String? = null,
    @SerialName("product_qty") var productQty: String? = null,
    @SerialName("order_payment_state") var orderPaymentState: String? = null,
    @SerialName("payment_state") var paymentState: String? = null,
    @SerialName("billing_country_code") var billingCountryCode: String? = null,
    @SerialName("shipping_country_code") var shippingCountryCode: String? = null,
    @SerialName("base_currency") var baseCurrency: String? = null,
    @SerialName("base_to_global_rate") var baseToGlobalRate: String? = null,
    @SerialName("items_commission") var itemsCommission: String? = null,
    @SerialName("shipping_amount") var shippingAmount: String? = null,
    @SerialName("base_shipping_amount") var baseShippingAmount: String? = null,
    @SerialName("shipping_paid") var shippingPaid: String? = null,
    @SerialName("shipping_refunded") var shippingRefunded: String? = null,
    @SerialName("method") var method: String? = null,
    @SerialName("method_title") var methodTitle: String? = null,
    @SerialName("carrier") var carrier: String? = null,
    @SerialName("carrier_title") var carrierTitle: String? = null,
    @SerialName("code") var code: String? = null,
    @SerialName("shipping_description") var shippingDescription: String? = null,
    @SerialName("vorders_mode") var vordersMode: String? = null,
    @SerialName("billing_name") var billingName: String? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("real_order_id") var realOrderId: String? = null,
    @SerialName("real_order_status") var realOrderStatus: String? = null,
    @SerialName("website_id") var websiteId: String? = null,
    @SerialName("b2b_pravno_lice") var b2bPravnoLice: String? = null,
    @SerialName("order_accepted_date") var orderAcceptedDate: String? = null,
    @SerialName("customer_id") var customerId: String? = null,
    @SerialName("reject_reason") var rejectReason: String? = null,
    @SerialName("id_broj") var idNumber: String? = null,
    @SerialName("pdv_broj") var pdvNumber: String? = null,
    @SerialName("products") var products: List<VendorProduct> = emptyList(),
    @SerialName("address") var address: ShopAddress? = null,
    @SerialName("shipping_details") var shippingDetails: ShippingDetails? = null,
    @SerialName("has_changes") var hasChanges: String? = null,
    @SerialName("is_locked") var locked: Boolean? = null,
) {
    fun locked() = locked ?: false
    fun totalAmount(): String {
        return karikaPriceFormat(orderTotal?.toDouble() ?: 0.00)
    }

    fun totalAmountWithPdv(): String {
        return karikaPriceFormat((orderTotal?.toDouble() ?: 0.00) * 1.17)
    }

    fun date() = createdAt?.toDate1() ?: "-"

    fun shouldShowShipping(): Boolean {
        return when (this.realOrderStatus) {
            "rejected" -> false
            "approved" -> false
            "cancelled" -> false
            "bill-sent" -> true
            "estimate-sent" -> true
            "pending" -> true
            else -> false
        }
    }

    fun isApproved(): Boolean {
        return when (this.realOrderStatus) {
            "rejected" -> false
            "approved" -> true
            "cancelled" -> false
            "bill-sent" -> false
            "estimate-sent" -> false
            "pending" -> false
            else -> false
        }
    }

    fun isRejected(): Boolean {
        return when (this.realOrderStatus) {
            "rejected" -> true
            "approved" -> false
            "cancelled" -> false
            "bill-sent" -> false
            "estimate-sent" -> false
            "pending" -> false
            else -> false
        }
    }

    fun status(): String {
        return when (this.realOrderStatus) {
            "rejected" -> "Odbijeno"
            "approved" -> "Odobreno"
            "cancelled" -> "Otkazano"
            "bill-sent" -> "Uplaćena"
            "estimate-sent" -> "Čekanje na uplatu"
            "pending" -> "Na čekanju"
            else -> "Nepoznato"
        }
    }

    fun statusColor(): Color {
        return when (realOrderStatus) {
            "rejected" -> KarikaColors.Red4_10
            "approved" -> KarikaColors.Approved
            "cancelled" -> KarikaColors.Gray2.copy(alpha = 0.1f)
            "bill-sent" -> KarikaColors.Orange1.copy(alpha = 0.1f)
            "estimate-sent" -> KarikaColors.Orange1.copy(alpha = 0.1f)
            "pending" -> KarikaColors.Blue3_10
            else -> KarikaColors.Pending
        }
    }

    fun statusTextColor(): Color {
        return when (realOrderStatus) {
            "rejected" -> KarikaColors.Red4
            "approved" -> KarikaColors.Green3
            "cancelled" -> KarikaColors.Gray2
            "bill-sent" -> KarikaColors.Orange1
            "estimate-sent" -> KarikaColors.Orange1
            "pending" -> KarikaColors.Blue3
            else -> KarikaColors.Orange
        }
    }

    fun hasChanges(): Boolean {
        return "1" == hasChanges
    }

    fun totalCommission(): String {
        val double = shopCommissionFee?.toDoubleOrNull() ?: 0.0
        return "${karikaPriceFormat(double)} KM"
    }

    fun isPending(): Boolean {
        return when (this.realOrderStatus) {
            "rejected" -> false
            "approved" -> false
            "cancelled" -> false
            "bill-sent" -> false
            "estimate-sent" -> false
            "pending" -> true
            else -> false
        }
    }

    fun email() = address?.email ?: "-"
}

@Serializable
data class VendorProduct(
    @SerialName("item_id") var itemId: String? = null,
    @SerialName("product_id") var productId: String? = null,
    @SerialName("name") var name: String? = null,
    @SerialName("price") var price: String? = null,
    @SerialName("original_price") var originalPrice: String? = null,
    @SerialName("row_total") var rowTotal: String? = null,
    @SerialName("mp_reward_earn") var mpRewardEarn: String? = null,
    @SerialName("sku") var sku: String? = null,
    @SerialName("qty_ordered") var qtyOrdered: String? = null,
    @SerialName("original_qty_ordered") var originalQty: String? = null,
    @SerialName("commission_percent") var commissionPercent: String? = null,
    @SerialName("commission") var commission: String? = null,
    @SerialName("description") var description: String? = null,
    @SerialName("short_description") var shortDescription: String? = null,
    @SerialName("weight") var weight: String? = null,
    @SerialName("qty") var qty: String? = null,
    @SerialName("manage_stock") var manageStock: String? = null,
    @SerialName("use_config_manage_stock") var useConfigManageStock: String? = null,
    @SerialName("salable_qty") var salableQty: String? = null,
    @SerialName("is_in_stock") var isInStock: String? = null,
    @SerialName("status") var status: String? = null,
    @SerialName("b2b_min_qty") var minQty: String? = null,
    @SerialName("min_quantity_unit") var minQtyUnit: String? = null,
    @SerialName("special_price") var specialPrice: String? = null,
    @SerialName("special_to_date") var specialPriceTo: String? = null,
    @SerialName("special_from_date") var specialPriceFrom: String? = null,
    @SerialName("image") var image: String? = null,
    @SerialName("thumbnail") var thumbnail: String? = null,
    @SerialName("small_image") var smallImage: String? = null,
    @SerialName("swatch_image") var swatchImage: String? = null,
    @SerialName("media_gallery") var media: List<MediaGallery>? = null,
    @SerialName("news_from_date") var newsFrom: String? = null,
    @SerialName("news_to_date") var newsTo: String? = null,
    @SerialName("categories") var categories: List<String> = emptyList(),
    @SerialName("is_karika_exclusive") var isExclusive: String? = "0",
    @SerialName("enable_messaging") var enabledMessages: String? = "0",
    @SerialName("product_unit") var unit: String? = "0",
    @SerialName("original_discount_percent") var originalDiscount: String? = null,
    @SerialName("discount_percent") var discount: String? = "0",
    @SerialName("discount_amount") var discountAmount: String? = "0",
) {
    val mediaGallery: List<MediaGallery>
        get() = media ?: emptyList()

    fun approved() = if (status == null) true else status == "1"
    fun isInStockLabel(): String {
        if (!approved()) {
            return "Čeka odobrenje"
        }
        return if (isInStock == "1") "Na zalihama" else "Nije na zalihama"
    }

    fun isInStockColor() = if (isInStock == "1") KarikaColors.Approved else KarikaColors.Red4_10
    fun isInStockTextColor() = if (isInStock == "1") KarikaColors.Green3 else KarikaColors.Red4

    fun salableQtyLabel(): String {
        if (!approved()) {
            return ""
        }
        return "kol: ${salableQty ?: "0"}"
    }

    fun priceVpc(): String {
        val double = price?.toDoubleOrNull() ?: 0.0
        return karikaPriceFormat(double) + " KM"
    }

    fun totalVpc(): String {
        val double = price?.toDoubleOrNull() ?: 0.0
        val rabat = discountAmount?.toDoubleOrNull() ?: 0.0
        return karikaPriceFormat((double * (qtyOrdered?.toIntOrNull() ?: 1)) - rabat) + " KM"
    }

    fun totalWithPdv(): String {
        val double = price?.toDoubleOrNull() ?: 0.0
        val rabat = discountAmount?.toDoubleOrNull() ?: 0.0
        return karikaPriceFormat(
            ((double * (qtyOrdered?.toIntOrNull() ?: 1) - rabat) * 1.17)
        ) + " KM"
    }

    fun qty(): String {
        return (qtyOrdered ?: "0") + " $unit"
    }

    fun originalQty(): String {
        if (originalQty == null) {
            return ""
        }
        return if (originalQty == qtyOrdered) "" else (originalQty ?: "0") + " $unit"
    }

    fun rabat(): String {
        return discount ?: "0"
    }

    fun originalRabat(): String {
        if (rabat() == "0") {
            return ""
        }
        return originalDiscount?.toIntOrNull()?.toString() ?: "0"
    }

    fun commissionPercent(): String {
        return karikaPriceFormat(commissionPercent?.toDoubleOrNull() ?: 0.0)
    }

    fun commission(): String {
        return karikaPriceFormat(commission?.toDoubleOrNull() ?: 0.0) + " KM"
    }

    fun price(): String {
        return karikaPriceFormat(
            this.price?.toDoubleOrNull() ?: 0.0
        ) + " KM"
    }


    fun status(): String {
        return when (status) {
            "1" -> "Odobreno"
            "2" -> "Na čekanju"
            "3" -> "Obrisan"
            "0" -> "Nije Odobreno"
            else -> ""
        }
    }
}

@Serializable
data class MediaGallery(
    @SerialName("url") var url: String? = null,
    @SerialName("id") var id: String? = null,
    @SerialName("position") var position: String? = null,
    @Transient var uri: Uri? = null,
    @Transient var markAsDeleted: Boolean = false,
    @Transient var primary: Boolean = false,
    @Transient var fullUrl: String? = null,
    @Transient var data: ByteArray? = null,
    @Transient var filename: String? = null,
) {
    fun image() = if (url == null) data else imageUrl(url)
}

@Serializable
data class ShippingDetails(
    @SerialName("contact_name") var name: String? = null,
    @SerialName("email") var email: String? = null,
    @SerialName("telephone") var telephone: String? = null,
    @SerialName("city") var city: String? = null,
    @SerialName("street") var street: String? = null,
    @SerialName("postcode") var postcode: String? = null,
    @SerialName("package_width") var width: String? = null,
    @SerialName("package_depth") var depth: String? = null,
    @SerialName("package_height") var height: String? = null,
    @SerialName("package_weight") var weight: String? = null,
    @SerialName("note") var note: String? = null,
)

@Serializable
data class ShopAddress(
    @SerialName("id") var id: Int? = null,
    @SerialName("customer_id") var customerId: Int? = null,
    @SerialName("country_id") var countryId: String? = null,
    @SerialName("street") var street: String? = null,
    @SerialName("telephone") var telephone: String? = null,
    @SerialName("postcode") var postcode: String? = null,
    @SerialName("city") var city: String? = null,
    @SerialName("firstname") var firstname: String? = null,
    @SerialName("lastname") var lastname: String? = null,
    @SerialName("email") var email: String? = null,
)

@Serializable
data class Shop(
    @SerialName("id") var id: String? = null,
    @SerialName("b2b_pravno_lice") var name: String? = null
)

@Serializable
data class VendorProductCategory(
    @SerialName("id") var id: String = "",
    @SerialName("name") var name: String = "",
    @SerialName("children") var children: List<VendorProductCategory> = emptyList(),
    @Transient var checked: Boolean = false
)

@Serializable
data class VendorDeliveryServiceData(
    @SerialName("orderId") var id: String = "",
    @SerialName("name") var name: String = "",
    @SerialName("email") var email: String = "",
    @SerialName("telephone") var telephone: String = "",
    @SerialName("city") var city: String = "",
    @SerialName("street") var street: String = "",
    @SerialName("postcode") var postcode: String = "",
    @SerialName("weight") var weight: String = "",
    @SerialName("width") var width: String = "",
    @SerialName("height") var height: String = "",
    @SerialName("depth") var depth: String = "",
    @SerialName("note") var note: String = "",
    @SerialName("company_code") var companyCode: String = ""
)

@Serializable
data class AIResponse(
    @SerialName("shortDescription") var shortDescription: String,
    @SerialName("description") var description: String,
    @SerialName("images") var images: List<String>,
)