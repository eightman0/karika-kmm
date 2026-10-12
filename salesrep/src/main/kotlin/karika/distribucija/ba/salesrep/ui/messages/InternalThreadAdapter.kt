package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemInternalThreadBinding
import karika.distribucija.ba.salesrep.model.ChatConversation

/** Mirrors composeApp's SalesInternalMessagesView.kt - SalesConversationItem with the colleague's
 * role (salesStaffRole) as the subtitle. */
class InternalThreadAdapter(
    private val onClick: (ChatConversation) -> Unit
) : ListAdapter<ChatConversation, InternalThreadAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemInternalThreadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInternalThreadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(thread: ChatConversation) {
            val context = binding.root.context
            val isUnread = thread.isUnread()
            val name = thread.counterpartName ?: "-"

            // Redesign: same white card for read/unread; unread shows bolder text + blue badge.
            binding.root.setBackgroundResource(R.drawable.bg_k_msg_card)
            binding.iconAvatarContainer.setBackgroundResource(R.drawable.bg_k_avatar)
            binding.iconAvatar.setColorFilter(context.getColor(R.color.k_accent))
            binding.textAvatarInitials.text = name.messageInitials()

            binding.textName.text = name
            binding.textName.setTextColor(context.getColor(R.color.k_ink))
            binding.textName.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif" else "sans-serif-medium",
                if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
            )

            binding.textDate.text = thread.dateTimeLabel().orEmpty()
            binding.textDate.setTextColor(context.getColor(if (isUnread) R.color.k_accent else R.color.k_subtle))

            val role = context.staffRoleLabel(thread.counterpartType)
            binding.textRole.visibility = if (role.isNotBlank()) View.VISIBLE else View.GONE
            binding.textRole.text = role

            // Preview of the last message, "Nema poruka" while there is none (SalesConversationItem).
            val preview = thread.lastMessagePreview?.takeIf { it.isNotBlank() }
            binding.textLastMessage.visibility = View.VISIBLE
            binding.textLastMessage.text = preview ?: context.getString(R.string.k_msg_no_messages)
            binding.textLastMessage.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif-medium" else "sans-serif", android.graphics.Typeface.NORMAL
            )
            binding.textLastMessage.setTextColor(
                context.getColor(
                    when {
                        preview == null -> R.color.k_subtle
                        isUnread -> R.color.k_ink
                        else -> R.color.k_muted
                    }
                )
            )

            binding.textUnreadBadge.visibility = if (isUnread) View.VISIBLE else View.GONE
            if (isUnread) {
                binding.textUnreadBadge.text = context.getString(R.string.internal_messages_unread_format, thread.unreadCount)
            }

            binding.iconArrow.setColorFilter(context.getColor(R.color.k_subtle))

            binding.root.setOnClickListener { onClick(thread) }
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
