package karika.distribucija.ba.salesrep.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/** Mirrors composeApp's domain/model/Conversation.kt, scoped to what the text-only
 * customer/admin message screens actually render (no attachment/admin-transient-flag fields -
 * this module uses separate Fragments/ViewModels for customer vs admin instead of one
 * Component parameterized by an `admin` boolean). */
@Serializable
data class Conversation(
    @SerialName("id") val id: String? = null,
    @SerialName("subject") val subject: String? = null,
    @SerialName("vendor_id") val vendorId: String? = null,
    @SerialName("receiver_id") val receiverId: String? = null,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("sender_name") val senderName: String? = null,
    @SerialName("receiver_name") val receiverName: String? = null,
    @SerialName("sender") val sender: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("unread") val unRead: String? = null,
    @SerialName("messages") val messages: List<List<Message>> = emptyList()
) {
    fun isRead(): Boolean = (unRead?.toIntOrNull() ?: 0) <= 0

    fun receiverIdInt(): Int? = if (receiverId == "0" || senderId == "0") 0 else vendorId?.toIntOrNull()

    fun date(): String? = updatedAt?.split(" ")?.getOrNull(0) ?: updatedAt

    fun customerName(): String = if (sender == "customer") senderName ?: "-" else receiverName ?: "-"

    /** Used by the admin-messages card instead of [customerName] - shows whichever side of the
     * thread isn't "0"/admin. */
    fun senderName(): String = when {
        sender == "customer" -> receiverName ?: "-"
        sender == "vendor" -> senderName ?: "-"
        senderId == "0" -> senderName ?: ""
        receiverId == "0" || receiverId.isNullOrBlank() -> receiverName ?: ""
        else -> "-"
    }
}

@Serializable
data class Message(
    @SerialName("id") val id: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("sender") val sender: String? = null,
    @SerialName("receiver_id") val receiverId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("images") val images: String? = null
) {
    fun isVendor() = sender == "vendor"

    /** Used by the admin conversation/new-message bubbles instead of [isVendor] - Admin's
     * Compose views classify "mine" by receiverId=="0"/sender=="customer" rather than
     * sender=="vendor" (see SalesAdminConversationView.kt/SalesAdminNewMessageView.kt's
     * `message.isVendorMessage()` vs Customer's `message.sender == "vendor"`). */
    fun isVendorMessage() = receiverId == "0" || sender == "customer"

    fun date(): String? = createdAt

    fun message(): String = emoticonsToEmoji(message ?: "")

    private fun emoticonsToEmoji(input: String): String {
        val map = mapOf(
            ":-)" to "🙂", ":)" to "😊", ":-(" to "☹️", ":(" to "☹️",
            ":D" to "😄", ";)" to "😉", ":P" to "😛", ":-P" to "😛"
        )
        var out = input
        map.forEach { (k, v) -> out = out.replace(k, v) }
        return out
    }
}

@Serializable
data class SendMessageRequest(
    val sendToAdmin: Boolean,
    val message: String,
    val subject: String?,
    val receiverId: Int?,
    val threadId: Int?,
    /** (filename, bytes) of a pending attachment, if any - never JSON-encoded, pulled out
     * manually when building the multipart body, matching composeApp's identical `@Transient`
     * field on its own SendMessageRequest. */
    @Transient val file: Pair<String?, ByteArray?>? = null
)

@Serializable
data class SendMessageResponse(
    @SerialName("error") val error: Boolean? = null,
    @SerialName("error_message") val errorMessage: String? = null,
    @SerialName("success") val success: Boolean? = null,
    @SerialName("success_message") val successMessage: String? = null,
    @SerialName("thread_id") val threadId: String? = null
)

/** Decode-only - never constructed/encoded client-side, only decoded from a Message's `images`
 * JSON-string field when rendering an already-sent attachment. */
@Serializable
data class FileData(val filename: List<String>? = null)
