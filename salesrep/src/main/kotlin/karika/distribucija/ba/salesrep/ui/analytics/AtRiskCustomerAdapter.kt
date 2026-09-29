package karika.distribucija.ba.salesrep.ui.analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemAtRiskCustomerBinding
import karika.distribucija.ba.salesrep.model.AtRiskCustomer
import karika.distribucija.ba.salesrep.model.CustomerRiskLevel
import karika.distribucija.ba.salesrep.util.karikaPriceFormat

/** Rows for the "Kupci koji zahtijevaju pažnju" list - one card per [AtRiskCustomer], with a
 * risk-level badge whose background/text color and label depend on [CustomerRiskLevel]. */
class AtRiskCustomerAdapter : ListAdapter<AtRiskCustomer, AtRiskCustomerAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAtRiskCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAtRiskCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AtRiskCustomer) {
            val context = binding.root.context

            binding.textDisplayId.text = item.displayId

            binding.textRiskBadge.text = context.getString(riskLabelRes(item.riskLevel))
            binding.textRiskBadge.background.setTint(context.getColor(riskBgRes(item.riskLevel)))
            binding.textRiskBadge.setTextColor(context.getColor(riskColorRes(item.riskLevel)))

            binding.textCustomerName.text = item.name
            binding.textSuspendedBadge.visibility = if (item.suspended) View.VISIBLE else View.GONE

            binding.textNote.text = item.note
            binding.textNote.visibility = if (item.note != null) View.VISIBLE else View.GONE

            binding.textStatInactive.text = "${item.inactiveDays} d"
            binding.textStatLastOrder.text = item.lastOrderDate
            binding.textStatAvgValue.text = "${karikaPriceFormat(item.avgOrderValue)} KM"
            binding.textStatTurnover.text = "${karikaPriceFormat(item.turnover)} KM"
        }

        private fun riskLabelRes(level: CustomerRiskLevel): Int = when (level) {
            CustomerRiskLevel.APPROACHING_RISK -> R.string.analytics_filter_approaching
            CustomerRiskLevel.AT_RISK -> R.string.analytics_filter_at_risk
            CustomerRiskLevel.SERIOUSLY_OVERDUE -> R.string.analytics_filter_overdue
            CustomerRiskLevel.NEVER_ORDERED -> R.string.analytics_filter_never_ordered
        }

        private fun riskBgRes(level: CustomerRiskLevel): Int = when (level) {
            CustomerRiskLevel.APPROACHING_RISK -> R.color.karika_yellow1
            CustomerRiskLevel.AT_RISK -> R.color.order_status_bg_warning
            CustomerRiskLevel.SERIOUSLY_OVERDUE -> R.color.karika_red2
            CustomerRiskLevel.NEVER_ORDERED -> R.color.karika_gray9
        }

        private fun riskColorRes(level: CustomerRiskLevel): Int = when (level) {
            CustomerRiskLevel.APPROACHING_RISK -> R.color.karika_yellow2
            CustomerRiskLevel.AT_RISK -> R.color.karika_orange
            CustomerRiskLevel.SERIOUSLY_OVERDUE -> R.color.karika_red3
            CustomerRiskLevel.NEVER_ORDERED -> R.color.karika_gray2
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AtRiskCustomer>() {
            override fun areItemsTheSame(oldItem: AtRiskCustomer, newItem: AtRiskCustomer) =
                oldItem.displayId == newItem.displayId

            override fun areContentsTheSame(oldItem: AtRiskCustomer, newItem: AtRiskCustomer) =
                oldItem == newItem
        }
    }
}
