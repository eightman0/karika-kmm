package karika.distribucija.ba.salesrep.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemNotificationBinding
import karika.distribucija.ba.salesrep.model.Notification

/** Mirrors composeApp's SalesNotificationItem in SalesNotificationsView.kt - only [Notification.body]
 * and [Notification.createdAt] are shown (title is never rendered there either, a genuine Compose
 * omission carried over here), with a pink wash + red dot while unread. */
class NotificationAdapter(
    private val onClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Notification) {
            val isUnread = item.isRead == "false"

            binding.rootNotificationItem.setBackgroundResource(
                if (isUnread) R.color.karika_red2 else android.R.color.transparent
            )
            binding.dotUnread.visibility = if (isUnread) View.VISIBLE else View.GONE
            binding.textNotificationBody.text = item.body
            binding.textNotificationTime.text = item.createdAt
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Notification>() {
            override fun areItemsTheSame(oldItem: Notification, newItem: Notification) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Notification, newItem: Notification) =
                oldItem == newItem
        }
    }
}
