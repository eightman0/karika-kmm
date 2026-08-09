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
            binding.textIncrementId.text = "#${order.incrementId}"
            binding.textCustomerName.text = order.displayName()
            binding.textDate.text = order.date()
            binding.textTotal.text = order.totalString()
            binding.textStatus.text = order.statusLabel()
            binding.textStatus.background.setTint(statusColor(order.status, binding.root.context))
            binding.root.setOnClickListener { onClick(order) }
        }

        private fun statusColor(status: String, context: android.content.Context): Int {
            val colorRes = when (status) {
                "pending" -> R.color.status_pending
                "processing" -> R.color.status_processing
                "approved" -> R.color.status_approved
                "bill-sent" -> R.color.status_bill_sent
                "estimate-sent" -> R.color.status_estimate_sent
                "rejected" -> R.color.status_rejected
                "cancelled" -> R.color.status_cancelled
                else -> R.color.status_default
            }
            return context.getColor(colorRes)
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
