package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemConversationCardBinding
import karika.distribucija.ba.salesrep.model.Conversation

private fun String?.formatDate(): String {
    if (this == null) return ""
    val datePart = this.split(" ").firstOrNull() ?: this
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}." else this
}

/** Mirrors composeApp's CustomerConversationCard in SalesCustomerMessagesView.kt - also reused for
 * admin messages, which render the same card but with [displayName] swapped to
 * `Conversation.senderName()` instead of `customerName()`. */
class ConversationCardAdapter(
    private val displayName: (Conversation) -> String = { it.customerName() },
    private val onClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationCardAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemConversationCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemConversationCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            val context = binding.root.context
            val isUnread = !conversation.isRead()

            // Redesign: same white card for read/unread; unread shows bolder text + blue badge.
            binding.root.setBackgroundResource(R.drawable.bg_k_msg_card)
            binding.iconAvatarContainer.setBackgroundResource(R.drawable.bg_k_avatar)
            binding.iconAvatar.setColorFilter(context.getColor(R.color.k_accent))

            val name = displayName(conversation)
            binding.textName.text = name
            binding.textAvatarInitials.text = name.messageInitials()
            binding.textName.setTextColor(context.getColor(R.color.k_ink))
            binding.textName.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif" else "sans-serif-medium",
                if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
            )

            binding.textDate.text = conversation.date().formatDate()
            binding.textDate.setTextColor(context.getColor(if (isUnread) R.color.k_accent else R.color.k_subtle))

            binding.textSubject.text = conversation.subject ?: "—"
            binding.textSubject.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif-medium" else "sans-serif", android.graphics.Typeface.NORMAL
            )
            binding.textSubject.setTextColor(context.getColor(if (isUnread) R.color.k_ink else R.color.k_muted))

            binding.textUnreadBadge.visibility = if (isUnread) View.VISIBLE else View.GONE

            binding.iconArrow.setColorFilter(context.getColor(R.color.k_subtle))

            binding.root.setOnClickListener { onClick(conversation) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation) =
                oldItem == newItem
        }
    }
}
