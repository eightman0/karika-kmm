package karika.distribucija.ba.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Conversation(
    @SerialName("id") var id: String? = null,
    @SerialName("subject") var subject: String? = null,
    @SerialName("vendor_id") var vendorId: String? = null,
    @SerialName("customer_id") var customerId: String? = null,
    @SerialName("receiver_id") var receiverId: String? = null,
    @SerialName("sender_id") var senderId: String? = null,
    @SerialName("sender_name") var senderName: String? = null,
    @SerialName("receiver_name") var receiverName: String? = null,
    @SerialName("sender") var sender: String? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("updated_at") var updatedAt: String? = null,
    @SerialName("unread") var unRead: String? = null,
    @SerialName("messages") var messages: List<List<Message>> = emptyList(),
    @Transient var admin: Boolean = false
) {
    fun isRead(): Boolean {
        return (unRead?.toIntOrNull() ?: 0) <= 0
    }

    fun senderName(): String {
        return when {
            sender == "customer" -> receiverName ?: "-"
            sender == "vendor" -> senderName ?: "-"
            senderId == "0" -> senderName ?: ""
            receiverId == "0" || receiverId.isNullOrBlank() -> receiverName ?: ""
            else -> "-"
        }
    }

    fun receiverId() = if (receiverId == "0" || senderId == "0") 0 else vendorId?.toIntOrNull()

    fun admin() = receiverId == "0" || senderId == "0"

    fun date(): String? {
        return updatedAt?.split(" ")?.getOrNull(0) ?: updatedAt
    }
}

@Serializable
data class Message(
    @SerialName("id") var id: String? = null,
    @SerialName("message") var message: String? = null,
    @SerialName("images") var images: String? = null,
    @SerialName("thread_id") var threadId: String? = null,
    @SerialName("customer_id") var customerId: String? = null,
    @SerialName("vendor_id") var vendorId: String? = null,
    @SerialName("sender_id") var senderId: String? = null,
    @SerialName("receiver_id") var receiverId: String? = null,
    @SerialName("sender") var sender: String? = null,
    @SerialName("created_at") var createdAt: String? = null,
    @SerialName("admin_status") var adminStatus: String? = null,
    @SerialName("receiver_status") var receiverStatus: String? = null,
    @SerialName("send_mail") var sendMail: String? = null
) {
    fun isVendorMessage() = receiverId == "0" || sender == "customer"

    fun isAdminMessage(): Boolean {
        return "0" == senderId
    }

    fun isMine(id: Int?): Boolean {
        if (sender == "vendor") return true
        if (senderId == id.toString()) return true

        return false
    }

    fun date(): String? {
        return createdAt
    }

    fun message(): String {
        return emoticonsToEmoji(message ?: "")
    }

    fun emoticonsToEmoji(input: String): String {
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
data class SendMessageRequest(
    @SerialName("send_to_admin") var sendToAdmin: Boolean,
    @SerialName("message") var message: String,
    @SerialName("subject") var subject: String?,
    @SerialName("receiver_id") var receiverId: Int?,
    @SerialName("thread_id") var threadId: Int?,
    @Transient var image: ByteArray? = null
)

@Serializable
data class SendMessageResponse(
    @SerialName("error") var error: Boolean? = null,
    @SerialName("error_message") var errorMessage: String? = null,
    @SerialName("success") var success: Boolean? = null,
    @SerialName("success_message") var successMessage: String? = null,
    @SerialName("thread_id") var threadId: String? = null

)

@Serializable
data class FileData(val filename: List<String>?)