package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsAtRiskBinding
import karika.distribucija.ba.salesrep.model.AtRiskCustomer
import karika.distribucija.ba.salesrep.model.AtRiskCustomers
import karika.distribucija.ba.salesrep.model.CustomerRiskLevel

/** "Kupci koji zahtijevaju pažnju" (at-risk customers) tab of the Analitika feature. Unlike the
 * other Analitika screens this has no filter-bar button of its own, but it still refetches
 * whenever filters change elsewhere via [AnalyticsFilterState.applyTrigger]. The 5 risk-level
 * pills only filter the already-fetched list locally/in-memory - they never trigger a refetch. */
class AnalyticsAtRiskFragment : Fragment() {

    private enum class RiskFilter(val riskLevel: CustomerRiskLevel?) {
        ALL(null),
        APPROACHING_RISK(CustomerRiskLevel.APPROACHING_RISK),
        AT_RISK(CustomerRiskLevel.AT_RISK),
        SERIOUSLY_OVERDUE(CustomerRiskLevel.SERIOUSLY_OVERDUE),
        NEVER_ORDERED(CustomerRiskLevel.NEVER_ORDERED),
    }

    private var _binding: FragmentAnalyticsAtRiskBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsAtRiskViewModel by viewModels()
    private lateinit var adapter: AtRiskCustomerAdapter

    private var selectedFilter: RiskFilter = RiskFilter.ALL
    private var latestCustomers: List<AtRiskCustomer> = emptyList()
    private var isLoading = false

    private val filterPills: List<Pair<RiskFilter, TextView>> by lazy {
        listOf(
            RiskFilter.ALL to binding.filterAll,
            RiskFilter.APPROACHING_RISK to binding.filterApproaching,
            RiskFilter.AT_RISK to binding.filterAtRisk,
            RiskFilter.SERIOUSLY_OVERDUE to binding.filterOverdue,
            RiskFilter.NEVER_ORDERED to binding.filterNeverOrdered,
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsAtRiskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AtRiskCustomerAdapter()
        binding.recyclerAtRiskCustomers.adapter = adapter
        binding.recyclerAtRiskCustomers.layoutManager = LinearLayoutManager(requireContext())

        filterPills.forEach { (filter, pill) ->
            pill.setOnClickListener { selectFilter(filter) }
        }
        updateFilterPillStyles()

        // The only place load() is triggered - fires immediately on first subscribe with
        // applyTrigger's current LiveData value, then again whenever filters change elsewhere.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) { viewModel.load() }

        viewModel.atRiskCustomers.observe(viewLifecycleOwner) { data ->
            latestCustomers = data.customers
            renderBuckets(data)
            renderList()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            renderList()
        }
    }

    private fun selectFilter(filter: RiskFilter) {
        if (filter == selectedFilter) return
        selectedFilter = filter
        updateFilterPillStyles()
        renderList()
    }

    private fun updateFilterPillStyles() {
        filterPills.forEach { (filter, pill) ->
            val selected = filter == selectedFilter
            pill.setBackgroundResource(
                if (selected) R.drawable.bg_chip_filter_active else R.drawable.bg_chip_filter_inactive
            )
            pill.setTextColor(
                requireContext().getColor(if (selected) R.color.karika_white else R.color.karika_gray2)
            )
        }
    }

    private fun renderBuckets(data: AtRiskCustomers) {
        binding.chipBucketLow.text = getString(R.string.analytics_bucket_30_59_format, data.bucket30to59)
        binding.chipBucketMid.text = getString(R.string.analytics_bucket_60_89_format, data.bucket60to89)
        binding.chipBucketHigh.text = getString(R.string.analytics_bucket_90_plus_format, data.bucket90plus)
    }

    private fun renderList() {
        val filtered = selectedFilter.riskLevel?.let { level ->
            latestCustomers.filter { it.riskLevel == level }
        } ?: latestCustomers
        adapter.submitList(filtered)
        binding.textAtRiskEmpty.visibility =
            if (filtered.isEmpty() && !isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
