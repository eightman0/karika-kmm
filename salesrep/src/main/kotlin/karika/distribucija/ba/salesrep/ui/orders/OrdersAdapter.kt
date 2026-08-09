package karika.distribucija.ba.salesrep.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemOrderBinding
import karika.distribucija.ba.salesrep.model.OnBehalfOrder

class OrdersAdapter(
    private val onClick: (OnBehalfOrder) -> Unit
) : ListAdapter<OnBehalfOrder, OrdersAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OnBehalfOrder) {
            binding.textCustomerName.text = order.displayName()
            binding.textIncrementId.text = "#${order.incrementId}"
            binding.textTotal.text = order.totalString()
            binding.textDate.text = order.date()
            binding.textStatus.text = order.statusLabel().uppercase()

            val context = binding.root.context
            binding.textStatus.background.setTint(context.getColor(statusBgRes(order.status)))
            binding.textStatus.setTextColor(context.getColor(statusColorRes(order.status)))

            binding.root.setOnClickListener { onClick(order) }
        }

        // Mirrors SalesOrdersView.kt's statusBg()/statusColor().
        private fun statusBgRes(status: String): Int = when (status) {
            "approved" -> R.color.order_status_bg_success
            "rejected" -> R.color.order_status_bg_error
            "cancelled" -> R.color.order_status_bg_neutral
            "pending", "processing" -> R.color.order_status_bg_info
            "bill-sent", "estimate-sent" -> R.color.order_status_bg_warning
            else -> R.color.order_status_bg_neutral
        }

        private fun statusColorRes(status: String): Int = when (status) {
            "approved" -> R.color.order_status_color_success
            "rejected" -> R.color.karika_error
            "cancelled" -> R.color.karika_gray6
            "pending", "processing" -> R.color.karika_blue
            "bill-sent", "estimate-sent" -> R.color.order_status_color_warning
            else -> R.color.karika_gray6
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<OnBehalfOrder>() {
            override fun areItemsTheSame(oldItem: OnBehalfOrder, newItem: OnBehalfOrder) =
                oldItem.orderId == newItem.orderId

            override fun areContentsTheSame(oldItem: OnBehalfOrder, newItem: OnBehalfOrder) =
                oldItem == newItem
        }
    }
}
