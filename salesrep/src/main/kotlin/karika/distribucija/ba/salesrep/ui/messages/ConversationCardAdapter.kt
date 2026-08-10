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

            binding.root.setBackgroundResource(
                if (isUnread) R.drawable.bg_thread_card_unread else R.drawable.bg_thread_card_read
            )
            binding.iconAvatarContainer.setBackgroundResource(
                if (isUnread) R.drawable.bg_avatar_circle_unread else R.drawable.bg_avatar_circle_read
            )
            binding.iconAvatar.setColorFilter(
                context.getColor(if (isUnread) R.color.karika_blue else R.color.karika_gray6)
            )

            binding.textName.text = displayName(conversation)
            binding.textName.setTextColor(
                context.getColor(if (isUnread) R.color.karika_gray2 else R.color.karika_gray6)
            )
            binding.textName.setTypeface(binding.textName.typeface, if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            binding.textName.textSize = if (isUnread) 15f else 14f

            binding.textDate.text = conversation.date().formatDate()
            binding.textDate.setTextColor(
                context.getColor(if (isUnread) R.color.karika_blue else R.color.karika_gray7)
            )

            binding.textSubject.text = conversation.subject ?: "—"
            binding.textSubject.setTypeface(binding.textSubject.typeface, if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            binding.textSubject.textSize = if (isUnread) 14f else 13f

            binding.textUnreadBadge.visibility = if (isUnread) View.VISIBLE else View.GONE

            binding.iconArrow.setColorFilter(
                context.getColor(if (isUnread) R.color.karika_blue else R.color.karika_gray9)
            )

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
