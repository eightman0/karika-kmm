package karika.distribucija.ba.salesrep.ui.analytics

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemRepPerformanceBinding
import karika.distribucija.ba.salesrep.model.SalesRepPerformance
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Row for the "Komercijalisti" (sales rep performance) screen: a numbered rank badge, the rep's
 * name (+ a "Suspendovan" pill when suspended), revenue, a goal-achievement progress bar and a
 * 4-column stats footer (orders/AOV/customers/growth). The rank number is the list position, not
 * part of [SalesRepPerformance] itself, so it's passed into [ViewHolder.bind] alongside the item.
 */
class RepPerformanceAdapter : ListAdapter<SalesRepPerformance, RepPerformanceAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRepPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class ViewHolder(private val binding: ItemRepPerformanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SalesRepPerformance, position: Int) {
            val context = binding.root.context

            binding.textRankBadge.text = (position + 1).toString()
            binding.textRepName.text = item.name
            binding.textSuspendedBadge.visibility = if (item.suspended) View.VISIBLE else View.GONE
            binding.textRevenue.text = "${karikaPriceFormat(item.revenue)} KM"

            val goalPercent = item.goalPercent
            binding.progressGoal.progress = goalPercent.coerceIn(0.0, 100.0).roundToInt()
            val goalColorRes = when {
                goalPercent >= 100.0 -> R.color.karika_green3
                goalPercent >= 70.0 -> R.color.karika_orange
                else -> R.color.karika_red
            }
            binding.progressGoal.progressTintList = ColorStateList.valueOf(context.getColor(goalColorRes))
            binding.textGoalLabel.text =
                context.getString(R.string.analytics_goal_label_format, goalPercent.formatPercent())

            binding.textOrders.text = item.orders.toString()
            binding.textAov.text = "${karikaPriceFormat(item.avgOrderValue)} KM"
            binding.textCustomers.text = "${item.activeCustomers} / ${item.totalCustomers}"

            val isPositiveGrowth = item.growthPercent >= 0.0
            val growthColor = context.getColor(if (isPositiveGrowth) R.color.karika_green3 else R.color.karika_red)
            binding.imageGrowthArrow.setImageResource(
                if (isPositiveGrowth) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
            )
            binding.imageGrowthArrow.imageTintList = ColorStateList.valueOf(growthColor)
            binding.textGrowth.setTextColor(growthColor)
            binding.textGrowth.text = "${abs(item.growthPercent).formatPercent()}%"
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SalesRepPerformance>() {
            override fun areItemsTheSame(oldItem: SalesRepPerformance, newItem: SalesRepPerformance) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: SalesRepPerformance, newItem: SalesRepPerformance) =
                oldItem == newItem
        }

        /** Trims to at most 2 decimals and drops trailing zeros, e.g. 82.0 -> "82", 12.5 -> "12,5".
         * Mirrors composeApp's distributer/analytics AnalyticsFormat.kt's formatNumber(), swapping
         * '.' for ',' to match this screen's Bosnian decimal-comma convention (as karikaPriceFormat
         * does for money). */
        private fun Double.formatPercent(): String {
            val cents = round(this * 100).toLong()
            val whole = cents / 100
            val frac = abs(cents % 100)
            return if (frac == 0L) "$whole" else "$whole,${frac.toString().padStart(2, '0').trimEnd('0')}"
        }
    }
}
