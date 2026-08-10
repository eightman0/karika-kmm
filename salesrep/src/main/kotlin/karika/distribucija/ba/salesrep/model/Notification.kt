package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors composeApp's domain/model/Notification.kt. */
@Serializable
data class Notification(
    @SerialName("entity_id") val id: String,
    @SerialName("customer_id") val customerId: String,
    @SerialName("title") val title: String,
    @SerialName("body") val body: String,
    @SerialName("route") val route: String,
    @SerialName("is_read") val isRead: String,
    @SerialName("created_at") val createdAt: String
)
