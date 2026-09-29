package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsOverviewBinding
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlin.math.abs
import kotlin.math.roundToInt

/** "Pregled" tab of the Analitika feature - stat cards summarizing revenue, orders, customers
 * and goal achievement for the currently selected [AnalyticsFilterState] range. */
class AnalyticsOverviewFragment : Fragment() {

    private var _binding: FragmentAnalyticsOverviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsOverviewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }
        binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()

        binding.cardAtRisk.setOnClickListener {
            findNavController().navigate(R.id.analyticsAtRiskFragment)
        }

        // Refreshes the filter-bar summary and (re)fetches the overview. This single observer
        // also fires once on first subscribe with the LiveData's current value, so load() must
        // not be called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.overview.observe(viewLifecycleOwner) { overview ->
            binding.textRevenueValue.text = "${karikaPriceFormat(overview.totalRevenue)} KM"
            bindDelta(binding.textRevenueDelta, overview.totalRevenueDelta)

            binding.textOrdersValue.text = overview.totalOrders.toString()
            bindDelta(binding.textOrdersDelta, overview.totalOrdersDelta)

            binding.textAvgOrderValue.text = "${karikaPriceFormat(overview.avgOrderValue)} KM"
            bindDelta(binding.textAvgOrderDelta, overview.avgOrderValueDelta)

            binding.textActiveCustomersValue.text = overview.activeCustomers.toString()
            bindDelta(binding.textActiveCustomersDelta, overview.activeCustomersDelta)

            binding.textNewCustomersValue.text = overview.newCustomers.toString()
            bindDelta(binding.textNewCustomersDelta, overview.newCustomersDelta)

            binding.textDistinctProductsValue.text = overview.distinctProducts.toString()
            bindDelta(binding.textDistinctProductsDelta, overview.distinctProductsDelta)

            binding.textRevenueGrowthValue.text = "${formatPercent(overview.revenueGrowth)}%"
            bindDelta(binding.textRevenueGrowthDelta, overview.revenueGrowthDelta)

            binding.textGoalAchievementValue.text = "${formatPercent(overview.goalAchievement)}%"
            binding.progressGoalAchievement.progress =
                overview.goalAchievement.roundToInt().coerceIn(0, 100)
            bindDelta(binding.textGoalAchievementDelta, overview.goalAchievementDelta)

            binding.textAtRiskValue.text = overview.atRiskCustomers.toString()
        }
    }

    /** One decimal, comma as the decimal separator, matching this app's Bosnian locale. */
    private fun formatPercent(value: Double): String =
        String.format("%.1f", value).replace('.', ',')

    /** Renders a percent-change value as "↑ +X,X% vs prethodni period" (green) when >= 0, or
     * "↓ X,X% vs prethodni period" (red) when negative. */
    private fun bindDelta(textView: TextView, delta: Double) {
        val magnitude = formatPercent(abs(delta))
        val vsPreviousPeriod = getString(R.string.analytics_vs_previous_period)
        if (delta >= 0) {
            textView.text = "↑ +$magnitude% $vsPreviousPeriod"
            textView.setTextColor(requireContext().getColor(R.color.karika_green3))
        } else {
            textView.text = "↓ $magnitude% $vsPreviousPeriod"
            textView.setTextColor(requireContext().getColor(R.color.karika_red))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
