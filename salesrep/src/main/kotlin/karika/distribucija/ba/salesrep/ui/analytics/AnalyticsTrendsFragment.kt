package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.patrykandpatrick.vico.views.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.views.cartesian.data.columnSeries
import com.patrykandpatrick.vico.views.cartesian.data.lineSeries
import com.patrykandpatrick.vico.views.common.data.ExtraStore
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsTrendsBinding
import karika.distribucija.ba.salesrep.model.AnalyticsSeriesPoint
import karika.distribucija.ba.salesrep.model.AnalyticsTrends
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlin.math.abs
import kotlinx.coroutines.launch

/** "Trendovi prodaje" tab of the Analitika feature - a revenue line chart, an orders column
 * chart, and two small trend-only mini charts for active/new customers, all backed by
 * [AnalyticsTrendsViewModel] and the shared [AnalyticsFilterState]. */
class AnalyticsTrendsFragment : Fragment() {

    private var _binding: FragmentAnalyticsTrendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsTrendsViewModel by viewModels()

    // One CartesianChartModelProducer per chart view, created once and fed via runTransaction()
    // whenever new AnalyticsTrends data arrives.
    private val revenueModelProducer = CartesianChartModelProducer()
    private val ordersModelProducer = CartesianChartModelProducer()
    private val activeCustomersModelProducer = CartesianChartModelProducer()
    private val newCustomersModelProducer = CartesianChartModelProducer()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chartRevenue.modelProducer = revenueModelProducer
        binding.chartOrders.modelProducer = ordersModelProducer
        binding.chartActiveCustomers.modelProducer = activeCustomersModelProducer
        binding.chartNewCustomers.modelProducer = newCustomersModelProducer

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }
        binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()

        // Refreshes the filter-bar summary and (re)fetches the trends. This single observer also
        // fires once on first subscribe with the LiveData's current value, so load() must not be
        // called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.trends.observe(viewLifecycleOwner) { trends ->
            renderTrends(trends)
        }
    }

    private fun renderTrends(trends: AnalyticsTrends) {
        binding.textRevenueTotal.text = "${karikaPriceFormat(trends.revenueTotal)} KM"
        binding.textRevenuePeak.text = "${karikaPriceFormat(trends.revenuePeak)} KM"

        val growthMagnitude = formatPercent(abs(trends.revenueGrowth))
        if (trends.revenueGrowth >= 0) {
            binding.textRevenueGrowth.text = "+$growthMagnitude%"
            binding.textRevenueGrowth.setTextColor(requireContext().getColor(R.color.karika_green3))
        } else {
            binding.textRevenueGrowth.text = "-$growthMagnitude%"
            binding.textRevenueGrowth.setTextColor(requireContext().getColor(R.color.karika_red))
        }

        binding.textOrdersTotal.text = trends.ordersTotal.toString()
        binding.textActiveCustomersValue.text = trends.activeCustomers.toString()
        binding.textNewCustomersValue.text = trends.newCustomers.toString()

        viewLifecycleOwner.lifecycleScope.launch {
            if (trends.revenueSeries.isNotEmpty()) {
                revenueModelProducer.runTransaction {
                    lineSeries { series(trends.revenueSeries.map { it.value }) }
                    extras { it[revenueLabelKey] = trends.revenueSeries.labels() }
                }
            }
            if (trends.ordersSeries.isNotEmpty()) {
                ordersModelProducer.runTransaction {
                    columnSeries { series(trends.ordersSeries.map { it.value }) }
                    extras { it[ordersLabelKey] = trends.ordersSeries.labels() }
                }
            }
            if (trends.activeCustomersSeries.isNotEmpty()) {
                activeCustomersModelProducer.runTransaction {
                    lineSeries { series(trends.activeCustomersSeries.map { it.value }) }
                    extras { it[activeCustomersLabelKey] = trends.activeCustomersSeries.labels() }
                }
            }
            if (trends.newCustomersSeries.isNotEmpty()) {
                newCustomersModelProducer.runTransaction {
                    lineSeries { series(trends.newCustomersSeries.map { it.value }) }
                    extras { it[newCustomersLabelKey] = trends.newCustomersSeries.labels() }
                }
            }
        }
    }

    private fun List<AnalyticsSeriesPoint>.labels(): List<String> = map { it.label }

    /** One decimal, comma as the decimal separator, matching this app's Bosnian locale (mirrors
     * AnalyticsOverviewFragment.formatPercent()). */
    private fun formatPercent(value: Double): String =
        String.format("%.1f", value).replace('.', ',')

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val revenueLabelKey = ExtraStore.Key<List<String>>()
        private val ordersLabelKey = ExtraStore.Key<List<String>>()
        private val activeCustomersLabelKey = ExtraStore.Key<List<String>>()
        private val newCustomersLabelKey = ExtraStore.Key<List<String>>()
    }
}
