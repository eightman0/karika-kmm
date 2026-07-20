package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StaffThread(
    @SerialName("thread_id") val threadId: Long,
    @SerialName("vendor_id") val vendorId: Long,
    @SerialName("counterpart_employee_id") val counterpartEmployeeId: Long,
    @SerialName("counterpart_name") val counterpartName: String,
    @SerialName("counterpart_role") val counterpartRole: String? = null,
    @SerialName("last_message") val lastMessage: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("updated_at") val updatedAt: String
) {
    fun hasUnread() = unreadCount > 0
    fun displayRole() = when (counterpartRole) {
        "vendor_admin" -> "Admin"
        "vendor_manager" -> "Menadžer"
        "sales_employee" -> "Komercijalista"
        else -> counterpartRole ?: ""
    }
}

@Serializable
data class StaffThreadSearchResults(
    @SerialName("items") val items: List<StaffThread> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)

@Serializable
data class StaffThreadMessage(
    @SerialName("message_id") val messageId: Long,
    @SerialName("thread_id") val threadId: Long,
    @SerialName("sender_employee_id") val senderEmployeeId: Long,
    @SerialName("sender_name") val senderName: String,
    @SerialName("message") val message: String,
    @SerialName("is_mine") val isMine: Boolean,
    @SerialName("read_at") val readAt: String? = null,
    @SerialName("created_at") val createdAt: String
) {
    fun formattedTime(): String {
        val timePart = createdAt.split("T").getOrNull(1) ?: createdAt.split(" ").getOrNull(1) ?: return ""
        val parts = timePart.split(":")
        return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
    }
}

@Serializable
data class StaffThreadMessageSearchResults(
    @SerialName("items") val items: List<StaffThreadMessage> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0,
)

@Serializable
data class StaffRecipient(
    @SerialName("employee_id") val employeeId: Long,
    @SerialName("name") val name: String,
    @SerialName("role") val role: String
) {
    fun displayRole() = when (role) {
        "vendor_admin" -> "Admin"
        "vendor_manager" -> "Menadžer"
        "sales_employee" -> "Komercijalista"
        else -> role
    }
}

@Serializable
data class StaffStartThread(
    val request: StaffStartThreadRequest
)

@Serializable
data class StaffStartThreadRequest(
    @SerialName("counterpart_employee_id") val counterpartEmployeeId: Long
)

@Serializable
data class StaffSendMessage(
    val request: StaffSendMessageRequest
)

@Serializable
data class StaffSendMessageRequest(
    @SerialName("message") val message: String
)
