package karika.distribucija.ba.salesrep.ui.analytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemCategoryPerformanceBinding
import karika.distribucija.ba.salesrep.model.AnalyticsCategory
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlin.math.abs

/** Row for the "Kategorije" tab of the Analitika "Proizvodi i kategorije" screen. Each row's
 * progress bar is sized relative to [maxRevenue] across the whole current category list - the
 * Fragment must set this (to the max revenue, coerced to at least 1.0) right before calling
 * [submitList]. */
class CategoryPerformanceAdapter : ListAdapter<AnalyticsCategory, CategoryPerformanceAdapter.ViewHolder>(DIFF) {

    var maxRevenue: Double = 1.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), maxRevenue)
    }

    class ViewHolder(private val binding: ItemCategoryPerformanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnalyticsCategory, maxRevenue: Double) {
            val context = binding.root.context
            binding.textCategoryName.text = item.name
            binding.textCategoryRevenue.text = "${karikaPriceFormat(item.revenue)} KM"
            binding.progressCategoryRevenue.progress =
                ((item.revenue / maxRevenue) * 100).toInt().coerceIn(0, 100)

            val growth = item.growthPercent
            val isPositive = growth >= 0
            val magnitude = formatPercent(abs(growth))
            val arrow = if (isPositive) "↑ +$magnitude%" else "↓ $magnitude%"
            binding.textCategorySubtitle.text = "${item.qty} kom   ${item.orders} narudžbi   $arrow"
            binding.textCategorySubtitle.setTextColor(
                context.getColor(if (isPositive) R.color.karika_green3 else R.color.karika_red)
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AnalyticsCategory>() {
            override fun areItemsTheSame(oldItem: AnalyticsCategory, newItem: AnalyticsCategory) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: AnalyticsCategory, newItem: AnalyticsCategory) =
                oldItem == newItem
        }
    }
}

/** One decimal, comma as the decimal separator - matches this app's Bosnian locale (mirrors
 * AnalyticsOverviewFragment's formatPercent()). */
private fun formatPercent(value: Double): String = String.format("%.1f", value).replace('.', ',')
