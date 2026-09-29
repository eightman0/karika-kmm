package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsProductsBinding

/** "Proizvodi i kategorije" tab of the Analitika feature - two switchable lists (best-selling
 * products and categories) for the currently selected [AnalyticsFilterState] range.
 *
 * Unlike the other Analitika screens this one has no filter-bar button of its own, but it still
 * refetches whenever filters change on the dedicated Filteri screen by observing
 * [AnalyticsFilterState.applyTrigger] - the only place [AnalyticsProductsViewModel.load] is
 * called, matching the other screens' single-observer convention. */
class AnalyticsProductsFragment : Fragment() {

    private var _binding: FragmentAnalyticsProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyticsProductsViewModel by viewModels()

    private lateinit var productAdapter: ProductPerformanceAdapter
    private lateinit var categoryAdapter: CategoryPerformanceAdapter

    private var productSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductPerformanceAdapter()
        binding.recyclerProducts.adapter = productAdapter
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())

        categoryAdapter = CategoryPerformanceAdapter()
        binding.recyclerCategories.adapter = categoryAdapter
        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonSegmentProducts.setOnClickListener { selectSegment(showProducts = true) }
        binding.buttonSegmentCategories.setOnClickListener { selectSegment(showProducts = false) }
        selectSegment(showProducts = true)

        binding.editSearchProducts.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            productSearchQuery = text?.toString().orEmpty()
            renderProducts()
        })

        // Refetches both lists whenever filters change on the Filteri screen. This single
        // observer also fires once on first subscribe with the LiveData's current value, so
        // load() must not be called anywhere else.
        AnalyticsFilterState.applyTrigger.observe(viewLifecycleOwner) { viewModel.load() }

        viewModel.products.observe(viewLifecycleOwner) { renderProducts() }
        viewModel.categories.observe(viewLifecycleOwner) { renderCategories() }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            renderProducts()
            renderCategories()
        }
    }

    private fun selectSegment(showProducts: Boolean) {
        binding.groupProducts.visibility = if (showProducts) View.VISIBLE else View.GONE
        binding.groupCategories.visibility = if (showProducts) View.GONE else View.VISIBLE

        binding.buttonSegmentProducts.setBackgroundResource(
            if (showProducts) R.drawable.bg_chip_filter_active else 0
        )
        binding.buttonSegmentProducts.setTextColor(
            requireContext().getColor(if (showProducts) R.color.karika_white else R.color.karika_gray6)
        )
        binding.buttonSegmentCategories.setBackgroundResource(
            if (!showProducts) R.drawable.bg_chip_filter_active else 0
        )
        binding.buttonSegmentCategories.setTextColor(
            requireContext().getColor(if (!showProducts) R.color.karika_white else R.color.karika_gray6)
        )
    }

    private fun renderProducts() {
        val query = productSearchQuery.trim()
        val filtered = viewModel.products.value.orEmpty().filter { product ->
            query.isEmpty() ||
                product.name.contains(query, ignoreCase = true) ||
                product.sku.contains(query, ignoreCase = true)
        }
        productAdapter.submitList(filtered)

        val isLoading = viewModel.isLoading.value == true
        binding.textProductsEmpty.visibility = if (filtered.isEmpty() && !isLoading) View.VISIBLE else View.GONE
    }

    private fun renderCategories() {
        val categories = viewModel.categories.value.orEmpty()
        categoryAdapter.maxRevenue = (categories.maxOfOrNull { it.revenue } ?: 1.0).coerceAtLeast(1.0)
        categoryAdapter.submitList(categories)

        val isLoading = viewModel.isLoading.value == true
        binding.textCategoriesEmpty.visibility = if (categories.isEmpty() && !isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
