package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KarikaTracking(
    @SerialName("session_id")
    val sessionId: String,
    @SerialName("user_type")
    val userType: UserType,
    val platform: String = "mobile",
    @SerialName("event_type")
    val eventType: EventType,
    val payload: TrackingPayload?,
    val url: String?,

    @SerialName("user_name")
    val userName: String? = null,
    @SerialName("user_email")
    val userEmail: String? = null
)

@Serializable
data class TrackingPayload(
    val ref: RefType,
    val product: String? = null,
    val sku: String? = null,
    val qty: Int? = null
)

enum class UserType(val value: String) {
    CUSTOMER("customer"),
    VENDOR("vendor")
}

enum class EventType(val value: String) {
    ADD_TO_CART("add_to_cart"),
    REMOVE_FROM_CART("remove_from_cart"),
    PRODUCT_FILTER("product_filter"),
    SEARCH_TERM("search_term"),
    USER_LOGIN("user_login"),
    LOGOUT("logout"),
    CUSTOMER_PASSWORD_CHANGE("customer_password_change"),
    PAGE_OPEN("page_open")
}

enum class RefType(val value: String) {
    CART_PANEL("cart_panel"),
    CATEGORY_PAGE("category_page"),
    SEARCH_PAGE("search_page"),
    VENDOR_PRODUCTS_PAGE("vendor_products_page"),
    SEARCH_BAR("search_bar"),
    USER_LOGIN("user_login"),
    LOGOUT("user_logout"),
}
