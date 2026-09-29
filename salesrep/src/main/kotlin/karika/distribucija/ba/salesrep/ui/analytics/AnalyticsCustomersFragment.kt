package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsCustomersBinding
import karika.distribucija.ba.salesrep.util.karikaPriceFormat
import kotlin.math.roundToInt

/** "Analitika kupaca" tab of the Analitika feature - stat cards summarizing assigned/active/new/
 * repeat customers, the repeat-purchase rate and per-customer revenue/order averages for the
 * currently selected [AnalyticsFilterState] range. */
class AnalyticsCustomersFragment : Fragment() {

    private var _binding: FragmentAnalyticsCustomersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsCustomersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }

        // Refreshes the filter-bar summary and (re)fetches the customer analytics. This single
        // observer also fires once on first subscribe with the LiveData's current value, so
        // load() must not be called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.customerAnalytics.observe(viewLifecycleOwner) { data ->
            binding.textAssignedCustomersValue.text = data.assignedCustomers.toString()
            binding.textActiveCustomersValue.text = data.activeCustomers.toString()
            binding.textNewCustomersValue.text = data.newCustomers.toString()
            binding.textRepeatCustomersValue.text = data.repeatCustomers.toString()

            binding.textRepeatRateValue.text = "${formatOneDecimal(data.repeatPurchaseRate)}%"
            binding.progressRepeatRate.progress = data.repeatPurchaseRate.roundToInt().coerceIn(0, 100)

            binding.textRevenuePerCustomerValue.text = "${karikaPriceFormat(data.revenuePerCustomer)} KM"
            binding.textAvgOrdersValue.text = formatOneDecimal(data.avgOrdersPerCustomer)

            binding.textCustomersTwoPlusValue.text = data.customersWith2PlusOrders.toString()
            binding.textCustomersNoneValue.text = data.customersWithoutOrders.toString()
        }
    }

    /** One decimal, comma as the decimal separator, matching this app's Bosnian locale (same
     * approach as AnalyticsOverviewFragment.formatPercent()). */
    private fun formatOneDecimal(value: Double): String =
        String.format("%.1f", value).replace('.', ',')

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
