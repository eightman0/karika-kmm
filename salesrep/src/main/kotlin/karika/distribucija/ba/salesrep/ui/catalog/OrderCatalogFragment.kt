package karika.distribucija.ba.salesrep.ui.catalog

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderCatalogBinding
import karika.distribucija.ba.salesrep.model.Category
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding

class OrderCatalogFragment : Fragment() {

    private var _binding: FragmentOrderCatalogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderCatalogViewModel by viewModels()
    private lateinit var adapter: ProductCatalogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val customerName = args.getString("customerName").orEmpty()
        val customerCompany = args.getString("customerCompany")
        val customerEmail = args.getString("customerEmail")
        val partnershipStatus = args.getString("partnershipStatus").orEmpty()
        val customerActive = args.getBoolean("customerActive")
        val hasShippingAddress = args.getBoolean("hasShippingAddress")
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.customers_order_for) + ": $customerName"

        adapter = ProductCatalogAdapter(
            lifecycleOwner = viewLifecycleOwner,
            getQty = { product -> viewModel.getCartQty(product) },
            onAdd = { product, qty -> viewModel.changeQty(product, qty) },
            onQuickView = { product ->
                AnalyticsTracker.trackClick("catalog", "quick_view")
                ProductQuickViewBottomSheet(product).show(parentFragmentManager, "product_quick_view")
            }
        )
        binding.recyclerProducts.adapter = adapter
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.applyImeBottomPadding()
        binding.recyclerProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (dy > 0 && lastVisible >= adapter.itemCount - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        binding.pillTabAll.setOnClickListener { selectTab(OrderCatalogViewModel.Tab.ALL) }
        binding.pillTabSale.setOnClickListener { selectTab(OrderCatalogViewModel.Tab.ON_SALE) }
        binding.pillTabPrevious.setOnClickListener { selectTab(OrderCatalogViewModel.Tab.PREVIOUSLY_ORDERED) }

        binding.editSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val query = text?.toString().orEmpty()
            binding.buttonClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            viewModel.setSearch(query)
        })
        binding.buttonClearSearch.setOnClickListener { binding.editSearch.setText("") }

        binding.buttonCategoryFilter.setOnClickListener { openCategorySheet() }
        binding.buttonCategoryChipRemove.setOnClickListener { viewModel.selectCategory(null) }

        binding.buttonCart.setOnClickListener {
            AnalyticsTracker.trackClick("catalog", "open_cart")
            findNavController().navigate(
                R.id.action_catalog_to_cart,
                bundleOf(
                    "customerId" to viewModel.customerId,
                    "customerName" to customerName,
                    "customerCompany" to customerCompany,
                    "customerEmail" to customerEmail,
                    "partnershipStatus" to partnershipStatus,
                    "customerActive" to customerActive,
                    "hasShippingAddress" to hasShippingAddress
                )
            )
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            renderEmptyState()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressLoading.visibility = if (it == true) View.VISIBLE else View.GONE
            binding.recyclerProducts.visibility = if (it == true) View.GONE else View.VISIBLE
            renderEmptyState()
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loadingMore ->
            binding.progressLoadingMore.visibility = if (loadingMore == true) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            val count = cart?.itemsCount ?: 0
            binding.textCartBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.textCartBadge.text = count.toString()
            adapter.refreshQuantities()
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { renderCategoryFilter() }
    }

    private fun selectTab(tab: OrderCatalogViewModel.Tab) {
        AnalyticsTracker.trackClick("catalog", "tab_${tab.name.lowercase()}")
        viewModel.selectTab(tab)
        stylePill(binding.pillTabAll, tab == OrderCatalogViewModel.Tab.ALL)
        stylePill(binding.pillTabSale, tab == OrderCatalogViewModel.Tab.ON_SALE)
        stylePill(binding.pillTabPrevious, tab == OrderCatalogViewModel.Tab.PREVIOUSLY_ORDERED)
        renderEmptyState()
        renderCategoryFilter()
    }

    private fun openCategorySheet(initialNavStack: List<Category> = emptyList()) {
        CategoryFilterBottomSheet(
            allCategories = viewModel.categories.value.orEmpty(),
            selectedCategory = viewModel.selectedCategory.value,
            initialNavStack = initialNavStack,
            onSelect = { category -> viewModel.selectCategory(category) }
        ).show(parentFragmentManager, "category_filter")
    }

    /** Mirrors the Compose catalog's category filter button + active-category chip, both only
     * shown on the ALL tab (matches SalesOrderCatalogView.kt's ALL_ITEMS-only visibility). */
    private fun renderCategoryFilter() {
        val isAllTab = viewModel.tab.value == OrderCatalogViewModel.Tab.ALL
        val selected = viewModel.selectedCategory.value

        binding.buttonCategoryFilter.visibility = if (isAllTab) View.VISIBLE else View.GONE
        binding.buttonCategoryFilter.setBackgroundResource(
            if (selected != null) R.drawable.bg_catalog_category_button_active else R.drawable.bg_catalog_category_button_inactive
        )
        binding.iconCategoryFilter.setColorFilter(
            requireContext().getColor(if (selected != null) R.color.karika_white else R.color.karika_gray6)
        )

        val showChip = isAllTab && selected != null
        binding.rowCategoryChip.visibility = if (showChip) View.VISIBLE else View.GONE
        if (showChip && selected != null) {
            renderCategoryBreadcrumb(categoryPath(viewModel.categories.value.orEmpty(), selected))
        }
    }

    private fun renderCategoryBreadcrumb(path: List<Category>) {
        val container = binding.layoutCategoryBreadcrumb
        container.removeAllViews()
        path.forEachIndexed { index, category ->
            val isLast = index == path.lastIndex
            container.addView(TextView(requireContext()).apply {
                text = category.name
                setTextColor(requireContext().getColor(R.color.karika_blue))
                textSize = 12f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                if (!isLast) {
                    isClickable = true
                    isFocusable = true
                    setOnClickListener { openCategorySheet(path.subList(0, index + 1).toList()) }
                }
            })
            if (!isLast) {
                container.addView(TextView(requireContext()).apply {
                    text = "->"
                    setTextColor(requireContext().getColor(R.color.karika_blue))
                    textSize = 12f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setPadding(dp(4), 0, dp(4), 0)
                })
            }
        }
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics
    ).toInt()

    private fun stylePill(pill: android.widget.TextView, selected: Boolean) {
        pill.setBackgroundResource(
            if (selected) R.drawable.bg_catalog_tab_selected else R.drawable.bg_catalog_tab_unselected
        )
        pill.setTextColor(requireContext().getColor(if (selected) R.color.karika_white else R.color.karika_gray2))
    }

    private fun renderEmptyState() {
        val isLoading = viewModel.isLoading.value == true
        val isEmpty = viewModel.products.value.orEmpty().isEmpty()
        binding.layoutEmpty.visibility = if (!isLoading && isEmpty) View.VISIBLE else View.GONE
        binding.textEmpty.text = getString(
            when (viewModel.tab.value) {
                OrderCatalogViewModel.Tab.PREVIOUSLY_ORDERED -> R.string.catalog_empty_previous
                OrderCatalogViewModel.Tab.ON_SALE -> R.string.catalog_empty_sale
                else -> R.string.catalog_empty
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
