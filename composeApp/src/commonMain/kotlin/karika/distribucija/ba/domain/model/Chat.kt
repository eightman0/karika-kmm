package karika.distribucija.ba.domain.model

import karika.distribucija.ba.util.inSarajevo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChatAxis {
    @SerialName("vendor_customer") VENDOR_CUSTOMER,
    @SerialName("customer_admin") CUSTOMER_ADMIN,
    @SerialName("vendor_admin") VENDOR_ADMIN,
    @SerialName("staff") STAFF
}

@Serializable
data class ChatStatus(
    @SerialName("enabled") val enabled: Boolean = false
)

@Serializable
data class ChatConversation(
    @SerialName("conversation_id") val conversationId: Long? = null,
    @SerialName("axis") val axis: ChatAxis? = null,
    @SerialName("counterpart_name") val counterpartName: String? = null,
    @SerialName("counterpart_type") val counterpartType: String? = null,
    @SerialName("counterpart_id") val counterpartId: Long? = null,
    @SerialName("last_message_preview") val lastMessagePreview: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("sender_employee_name") val senderEmployeeName: String? = null
) {
    fun isUnread() = unreadCount > 0

    fun dateTimeLabel(): String? {
        val raw = lastMessageAt?.inSarajevo() ?: return null
        val segments = raw.split(" ", limit = 2)
        val dateParts = segments.getOrNull(0)?.split("-")
        if (dateParts?.size != 3) return raw

        val (year, month, day) = dateParts
        val date = "${day.padStart(2, '0')}.${month.padStart(2, '0')}.$year."
        val time = segments.getOrNull(1)?.take(5)
        return if (time != null) "$date $time" else date
    }
}

@Serializable
data class ChatConversationSearchResults(
    @SerialName("items") val items: List<ChatConversation> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0
)

@Serializable
data class ChatMessage(
    @SerialName("message_id") val messageId: Long? = null,
    @SerialName("conversation_id") val conversationId: Long? = null,
    @SerialName("sender_type") val senderType: String? = null,
    @SerialName("sender_ref_id") val senderRefId: Long? = null,
    @SerialName("sender_display_name") val senderDisplayName: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("attachments") val attachments: List<ChatAttachment> = emptyList()
) {
    fun isFromCustomer() = senderType == "customer"

    fun isFromVendor() = senderType == "vendor_owner" || senderType == "vendor_employee"

    fun message(): String {
        return emoticonsToEmoji(body ?: "")
    }

    private fun emoticonsToEmoji(input: String): String {
        val map = mapOf(
            ":-)" to "🙂",
            ":)" to "😊",
            ":-(" to "☹️",
            ":(" to "☹️",
            ":D" to "😄",
            ";)" to "😉",
            ":P" to "😛",
            ":-P" to "😛"
        )
        var out = input
        map.forEach { (k, v) ->
            out = out.replace(k, v)
        }
        return out
    }
}

@Serializable
data class ChatMessageSearchResults(
    @SerialName("items") val items: List<ChatMessage> = emptyList(),
    @SerialName("total_count") val totalCount: Int = 0
)

@Serializable
data class ChatAttachment(
    @SerialName("attachment_id") val attachmentId: Long? = null,
    @SerialName("filename") val filename: String? = null,
    @SerialName("mime") val mime: String? = null,
    @SerialName("size") val size: Long? = null,
    @SerialName("relpath") val relpath: String? = null
) {
    fun isPdf() = mime?.contains("pdf") == true || filename?.endsWith(".pdf", ignoreCase = true) == true
}

@Serializable
data class ChatRecipient(
    @SerialName("axis") val axis: ChatAxis? = null,
    @SerialName("counterpart_id") val counterpartId: Long? = null,
    @SerialName("counterpart_type") val counterpartType: String? = null,
    @SerialName("name") val name: String? = null
)

@Serializable
data class ChatUnreadCount(
    @SerialName("total") val total: Int = 0,
    @SerialName("vendor_customer") val vendorCustomer: Int = 0,
    @SerialName("customer_admin") val customerAdmin: Int = 0,
    @SerialName("vendor_admin") val vendorAdmin: Int = 0,
    @SerialName("staff") val staff: Int = 0
)

@Serializable
data class ChatStartRequest(
    @SerialName("axis") val axis: ChatAxis,
    @SerialName("counterpart_id") val counterpartId: Long? = null
)

@Serializable
data class ChatStartBody(
    @SerialName("request") val request: ChatStartRequest
)

@Serializable
data class ChatFile(
    @SerialName("filename") val filename: String,
    @SerialName("mime") val mime: String? = null,
    @SerialName("content") val content: String
)

@Serializable
data class ChatSendRequest(
    @SerialName("message") val message: String,
    @SerialName("files") val files: List<ChatFile> = emptyList()
)

@Serializable
data class ChatSendBody(
    @SerialName("request") val request: ChatSendRequest
)
