package karika.distribucija.ba.salesrep.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemNotificationBinding
import karika.distribucija.ba.salesrep.model.VendorNotification
import karika.distribucija.ba.salesrep.util.inSarajevo

/** Mirrors composeApp's SalesNotificationItem in SalesNotificationsView.kt - only [VendorNotification.body]
 * and [VendorNotification.createdAt] are shown (title is never rendered there either, a genuine Compose
 * omission carried over here), with a blue wash, blue bell tile + blue dot while unread. */
class NotificationAdapter(
    private val onClick: (VendorNotification) -> Unit
) : ListAdapter<VendorNotification, NotificationAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VendorNotification) {
            val isUnread = !item.isRead

            val context = binding.root.context
            binding.rootNotificationItem.setBackgroundResource(
                if (isUnread) R.drawable.bg_k_shell_notification_unread else R.drawable.bg_k_shell_notification_read
            )
            binding.tileKShellNotificationIcon.setBackgroundResource(
                if (isUnread) R.drawable.bg_k_icon_tile else R.drawable.bg_k_shell_tile_field
            )
            binding.iconKShellNotification.setColorFilter(
                context.getColor(if (isUnread) R.color.k_accent else R.color.k_muted)
            )
            binding.textNotificationBody.typeface = android.graphics.Typeface.create(
                if (isUnread) "sans-serif" else "sans-serif-medium",
                if (isUnread) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
            )
            binding.dotUnread.visibility = if (isUnread) View.VISIBLE else View.GONE
            binding.textNotificationBody.text = item.body
            binding.textNotificationTime.text = item.createdAt.inSarajevo()
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VendorNotification>() {
            override fun areItemsTheSame(oldItem: VendorNotification, newItem: VendorNotification) =
                oldItem.notificationId == newItem.notificationId

            override fun areContentsTheSame(oldItem: VendorNotification, newItem: VendorNotification) =
                oldItem == newItem
        }
    }
}
