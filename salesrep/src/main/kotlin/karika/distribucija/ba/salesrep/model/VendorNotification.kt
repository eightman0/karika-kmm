package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors composeApp's domain/model/Notification.kt VendorNotification - the
 * `/V1/vendor-operations/notifications` (Chat V2 mobile spec) feed. */
@Serializable
data class VendorNotification(
    @SerialName("notification_id") val notificationId: Long,
    @SerialName("title") val title: String,
    @SerialName("body") val body: String,
    @SerialName("route") val route: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
data class VendorNotificationSearchResults(
    @SerialName("items") val items: List<VendorNotification> = emptyList(),
    @SerialName("total_count") val totalCount: Long = 0,
)
