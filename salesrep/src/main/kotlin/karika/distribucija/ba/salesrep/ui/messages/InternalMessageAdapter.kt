package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemInternalMessageBinding
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.session.CurrentUser
import karika.distribucija.ba.salesrep.util.inSarajevo

/** Mirrors composeApp's SalesChatBubble with html = false, as used by both
 * SalesInternalConversationView.kt and SalesInternalNewMessageView.kt: "mine" is a message whose
 * sender_ref_id is the rep's own id (see [isMyMessage]), the other side's bubble is labelled with
 * the colleague's name. */
class InternalMessageAdapter(
    private val counterpartName: () -> String
) : ListAdapter<ChatMessage, InternalMessageAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemInternalMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInternalMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            val context = binding.root.context
            val isMine = message.isMyMessage()

            // Sender name only on the colleague's bubble, inside it (SalesChatBubble).
            val sender = if (isMine) "" else counterpartName()
            binding.textSenderLabel.visibility = if (sender.isNotBlank()) View.VISIBLE else View.GONE
            binding.textSenderLabel.text = sender

            binding.bubbleContainer.setBackgroundResource(
                if (isMine) R.drawable.bg_k_msg_bubble_mine else R.drawable.bg_k_msg_bubble_other
            )
            val body = message.body.orEmpty()
            binding.textMessageBubble.text = body
            binding.textMessageBubble.visibility = if (body.isEmpty()) View.GONE else View.VISIBLE
            binding.textMessageBubble.setTextColor(context.getColor(if (isMine) R.color.k_white else R.color.k_ink))

            // Time inside the bubble (formatChatTime: HH:mm in Sarajevo time).
            val time = message.createdAt.chatTime()
            binding.textMessageTime.visibility = if (time.isNotEmpty()) View.VISIBLE else View.GONE
            binding.textMessageTime.text = time
            binding.textMessageTime.setTextColor(
                context.getColor(if (isMine) R.color.k_msg_time_mine else R.color.k_subtle)
            )

            binding.bubbleContainer.alignBubble(isMine)
        }

        /** Mirrors composeApp's `formatChatTime()`: Sarajevo time, cut to HH:mm. */
        private fun String?.chatTime(): String {
            if (this == null) return ""
            val timePart = inSarajevo().split(" ").getOrNull(1) ?: return ""
            val parts = timePart.split(":")
            return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
                oldItem.messageId == newItem.messageId

            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
                oldItem == newItem
        }
    }
}

/** composeApp's SalesInternal*Component.isMine(): the vendor owner's messages carry the vendor
 * id, an employee's their employee id. */
internal fun ChatMessage.isMyMessage(): Boolean {
    val me = CurrentUser.me
    val myRefId = if (me?.isVendorOwner == true) me.vendorId ?: 0L else me?.employeeId ?: 0L
    return senderRefId == myRefId
}
