package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsRepsBinding
import karika.distribucija.ba.salesrep.util.karikaPriceFormat

/**
 * "Komercijalisti" (sales rep performance) screen: the filter bar, a ranked list of reps and a
 * footer card summarizing revenue from customers with no active sales-rep assignment. Mirrors
 * [karika.distribucija.ba.salesrep.ui.notifications.NotificationsFragment]'s shape.
 */
class AnalyticsRepsFragment : Fragment() {

    private var _binding: FragmentAnalyticsRepsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsRepsViewModel by viewModels()
    private lateinit var adapter: RepPerformanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsRepsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RepPerformanceAdapter()
        binding.recyclerReps.adapter = adapter
        binding.recyclerReps.layoutManager = LinearLayoutManager(requireContext())

        binding.filterBar.root.setOnClickListener {
            findNavController().navigate(R.id.analyticsFiltersFragment)
        }

        // The only place that triggers a (re)load: LiveData redelivers its last value to a new
        // observer, so this both refreshes the filter summary and refetches once per filter visit
        // (see AnalyticsFilterState's kdoc) as well as on this screen's first view creation.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) {
            binding.filterBar.textFilterSummary.text = AnalyticsFilterState.summaryText()
            viewModel.load()
        }

        viewModel.reps.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.reps)
            binding.textUnassignedRevenue.text = "${karikaPriceFormat(data.unassignedRevenue)} KM"
            updateEmptyState()
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            updateEmptyState()
        }
    }

    private fun updateEmptyState() {
        val isEmpty = viewModel.reps.value?.reps.isNullOrEmpty()
        val isLoading = viewModel.loading.value == true
        binding.textEmptyState.visibility = if (isEmpty && !isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
