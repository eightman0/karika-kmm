package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @SerialName("entity_id") var id: String,
    @SerialName("customer_id") var customerId: String,
    @SerialName("title") var title: String,
    @SerialName("body") var body: String,
    @SerialName("route") var route: String,
    @SerialName("is_read") var isRead: String,
    @SerialName("created_at") var createdAt: String,
)