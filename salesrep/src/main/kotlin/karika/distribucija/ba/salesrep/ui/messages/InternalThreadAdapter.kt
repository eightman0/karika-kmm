package karika.distribucija.ba.salesrep.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemInternalThreadBinding
import karika.distribucija.ba.salesrep.model.StaffThread

private fun String?.formatDate(): String {
    if (this == null) return ""
    val datePart = this.split("T").firstOrNull() ?: this.split(" ").firstOrNull() ?: this
    val parts = datePart.split("-")
    return if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}." else this
}

/** Mirrors composeApp's SalesInternalMessagesView.kt InternalThreadCard. */
class InternalThreadAdapter(
    private val onClick: (StaffThread) -> Unit
) : ListAdapter<StaffThread, InternalThreadAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemInternalThreadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemInternalThreadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(thread: StaffThread) {
            val context = binding.root.context
            val isUnread = thread.hasUnread()

            binding.root.setBackgroundResource(
                if (isUnread) R.drawable.bg_thread_card_unread else R.drawable.bg_thread_card_read
            )
            binding.iconAvatarContainer.setBackgroundResource(
                if (isUnread) R.drawable.bg_avatar_circle_unread else R.drawable.bg_avatar_circle_read
            )
            binding.iconAvatar.setColorFilter(
                context.getColor(if (isUnread) R.color.karika_blue else R.color.karika_gray6)
            )

            binding.textName.text = thread.counterpartName
            binding.textName.setTextColor(
                context.getColor(if (isUnread) R.color.karika_gray2 else R.color.karika_gray6)
            )
            binding.textName.setTypeface(binding.textName.typeface, if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            binding.textName.textSize = if (isUnread) 15f else 14f

            binding.textDate.text = (thread.lastMessageAt ?: thread.updatedAt).formatDate()
            binding.textDate.setTextColor(
                context.getColor(if (isUnread) R.color.karika_blue else R.color.karika_gray7)
            )

            binding.textRole.text = thread.displayRole()

            val hasLastMessage = !thread.lastMessage.isNullOrEmpty()
            binding.textLastMessage.visibility = if (hasLastMessage) View.VISIBLE else View.GONE
            if (hasLastMessage) {
                binding.textLastMessage.text = thread.lastMessage
                binding.textLastMessage.setTypeface(binding.textLastMessage.typeface, if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
                binding.textLastMessage.textSize = if (isUnread) 14f else 13f
            }

            binding.textUnreadBadge.visibility = if (isUnread) View.VISIBLE else View.GONE
            if (isUnread) {
                binding.textUnreadBadge.text = context.getString(R.string.internal_messages_unread_format, thread.unreadCount)
            }

            binding.iconArrow.setColorFilter(
                context.getColor(if (isUnread) R.color.karika_blue else R.color.karika_gray9)
            )

            binding.root.setOnClickListener { onClick(thread) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StaffThread>() {
            override fun areItemsTheSame(oldItem: StaffThread, newItem: StaffThread) =
                oldItem.threadId == newItem.threadId

            override fun areContentsTheSame(oldItem: StaffThread, newItem: StaffThread) =
                oldItem == newItem
        }
    }
}
