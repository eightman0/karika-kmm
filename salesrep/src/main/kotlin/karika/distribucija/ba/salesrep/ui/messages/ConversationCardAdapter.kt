package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemConversationCardBinding
import karika.distribucija.ba.salesrep.model.ChatConversation

/** Mirrors composeApp's SalesConversationItem in SalesChatUi.kt - used by the customer and admin
 * inboxes: counterpart name, Sarajevo date-time of the last message, its preview ("Nema poruka"
 * when there is none) and the unread badge. */
class ConversationCardAdapter(
    private val onClick: (ChatConversation) -> Unit
) : ListAdapter<ChatConversation, ConversationCardAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemConversationCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemConversationCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: ChatConversation) {
            val context = binding.root.context
            val isUnread = conversation.isUnread()

            // Redesign: same white card for read/unread; unread shows bolder text + blue badge.
            binding.root.setBackgroundResource(R.drawable.bg_k_msg_card)
            binding.iconAvatarContainer.setBackgroundResource(R.drawable.bg_k_avatar)
            binding.iconAvatar.setColorFilter(context.getColor(R.color.k_accent))

            val name = conversation.counterpartName ?: "-"
            binding.textName.text = name
            binding.textAvatarInitials.text = name.messageInitials()
            binding.textName.setTextColor(context.getColor(R.color.k_ink))
            binding.textName.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif" else "sans-serif-medium",
                if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
            )

            binding.textDate.text = conversation.dateTimeLabel().orEmpty()
            binding.textDate.setTextColor(context.getColor(if (isUnread) R.color.k_accent else R.color.k_subtle))

            val preview = conversation.lastMessagePreview?.takeIf { it.isNotBlank() }
            binding.textSubject.text = preview ?: context.getString(R.string.k_msg_no_messages)
            binding.textSubject.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif-medium" else "sans-serif", android.graphics.Typeface.NORMAL
            )
            binding.textSubject.setTextColor(
                context.getColor(
                    when {
                        preview == null -> R.color.k_subtle
                        isUnread -> R.color.k_ink
                        else -> R.color.k_muted
                    }
                )
            )

            binding.textUnreadBadge.visibility = if (isUnread) View.VISIBLE else View.GONE

            binding.iconArrow.setColorFilter(context.getColor(R.color.k_subtle))

            binding.root.setOnClickListener { onClick(conversation) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatConversation>() {
            override fun areItemsTheSame(oldItem: ChatConversation, newItem: ChatConversation) =
                oldItem.conversationId == newItem.conversationId

            override fun areContentsTheSame(oldItem: ChatConversation, newItem: ChatConversation) =
                oldItem == newItem
        }
    }
}
