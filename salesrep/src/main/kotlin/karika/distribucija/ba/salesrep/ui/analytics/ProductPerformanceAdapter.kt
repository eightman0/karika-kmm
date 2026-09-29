package karika.distribucija.ba.salesrep.ui.analytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.ItemProductPerformanceBinding
import karika.distribucija.ba.salesrep.model.AnalyticsProduct
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlin.math.abs

/** Row for the "Proizvodi" tab of the Analitika "Proizvodi i kategorije" screen. */
class ProductPerformanceAdapter : ListAdapter<AnalyticsProduct, ProductPerformanceAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductPerformanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemProductPerformanceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AnalyticsProduct) {
            val context = binding.root.context
            binding.textProductName.text = item.name
            binding.textProductSubtitle.text = "${item.sku} · ${item.qty} kom · ${item.orders} narudžbi"
            binding.textProductRevenue.text = "${karikaPriceFormat(item.revenue)} KM"

            val growth = item.growthPercent
            val isPositive = growth >= 0
            val magnitude = formatPercent(abs(growth))
            binding.textProductGrowth.text = if (isPositive) "↑ +$magnitude%" else "↓ $magnitude%"
            binding.textProductGrowth.setTextColor(
                context.getColor(if (isPositive) R.color.karika_green3 else R.color.karika_red)
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AnalyticsProduct>() {
            override fun areItemsTheSame(oldItem: AnalyticsProduct, newItem: AnalyticsProduct) =
                oldItem.sku == newItem.sku

            override fun areContentsTheSame(oldItem: AnalyticsProduct, newItem: AnalyticsProduct) =
                oldItem == newItem
        }
    }
}

/** One decimal, comma as the decimal separator - matches this app's Bosnian locale (mirrors
 * AnalyticsOverviewFragment's formatPercent()). */
private fun formatPercent(value: Double): String = String.format("%.1f", value).replace('.', ',')
