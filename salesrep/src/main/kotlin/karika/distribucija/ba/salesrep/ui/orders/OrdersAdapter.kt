package karika.distribucija.ba.salesrep.ui.orders

import android.content.res.ColorStateList
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
            binding.textStatus.text = order.statusLabel()

            val context = binding.root.context
            val statusColor = context.getColor(statusColorRes(order.status))
            binding.textStatus.background.mutate().setTint(context.getColor(statusBgRes(order.status)))
            binding.textStatus.setTextColor(statusColor)
            binding.textStatus.compoundDrawableTintList = ColorStateList.valueOf(statusColor)

            binding.root.setOnClickListener { onClick(order) }
        }

        // Mirrors SalesOrdersView.kt's statusPillColors().
        private fun statusBgRes(status: String): Int = when (status) {
            "approved", "bill-sent" -> R.color.k_green_soft
            "rejected" -> R.color.k_red_soft
            "processing" -> R.color.k_accent_soft
            "pending", "estimate-sent" -> R.color.k_amber_soft
            else -> R.color.k_field
        }

        private fun statusColorRes(status: String): Int = when (status) {
            "approved", "bill-sent" -> R.color.k_green
            "rejected" -> R.color.k_red
            "processing" -> R.color.k_accent
            "pending", "estimate-sent" -> R.color.k_amber
            else -> R.color.k_muted
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
