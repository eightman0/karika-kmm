package karika.distribucija.ba.salesrep.ui.orders

import karika.distribucija.ba.salesrep.util.showSnack
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrdersListBinding
import karika.distribucija.ba.salesrep.model.OnBehalfOrder

/** Mirrors composeApp's SalesOrdersView.kt: search, status chip row, order cards and the search/status-aware empty state. */
class OrdersListFragment : Fragment() {

    private var _binding: FragmentOrdersListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrdersViewModel by viewModels()
    private lateinit var adapter: OrdersAdapter
    private var searchWatcher: TextWatcher? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrdersAdapter(onClick = ::openOrder)
        binding.recyclerOrders.adapter = adapter
        binding.recyclerOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerOrders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (dy > 0 && lastVisible >= adapter.itemCount - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        // Restore the field after a config change / return from the detail screen without
        // re-triggering the debounced search.
        binding.editSearch.setText(viewModel.searchQuery)
        binding.iconClearSearch.visibility = if (viewModel.searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
        searchWatcher = binding.editSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val query = text?.toString().orEmpty()
            binding.iconClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            if (query != viewModel.searchQuery) viewModel.setSearch(query)
        })
        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                true
            } else false
        }
        binding.iconClearSearch.setOnClickListener { binding.editSearch.setText("") }

        binding.buttonReset.setOnClickListener {
            if (viewModel.searchQuery.isNotBlank()) binding.editSearch.setText("")
            if (viewModel.statusFilter != null) selectStatus(null)
        }

        renderFilter()

        viewModel.orders.observe(viewLifecycleOwner) { renderEmptyState() }
        viewModel.orders.observe(viewLifecycleOwner) { orders -> adapter.submitList(orders) }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
            renderEmptyState()
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loadingMore ->
            binding.progressLoadMore.visibility = if (loadingMore) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                showSnack(message)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun selectStatus(status: String?) {
        if (status == viewModel.statusFilter) return
        viewModel.setStatus(status)
        renderFilter()
    }

    /** Status chips: "Sve" + every status. */
    private fun renderFilter() {
        val selected = viewModel.statusFilter

        val row = binding.rowStatusChips
        row.removeAllViews()
        val options = listOf<Pair<String?, String>>(null to getString(R.string.orders_status_all)) + orderStatusOptions
        options.forEachIndexed { index, (value, label) ->
            row.addView(buildChip(label, value == selected) { selectStatus(value) }, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(34)
            ).apply { if (index > 0) marginStart = dp(8) })
        }
    }

    private fun buildChip(label: String, selected: Boolean, onClick: () -> Unit): TextView =
        TextView(requireContext()).apply {
            text = label
            gravity = Gravity.CENTER
            maxLines = 1
            setPadding(dp(14), 0, dp(14), 0)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            setTextColor(context.getColor(if (selected) R.color.k_white else R.color.k_ink))
            setBackgroundResource(if (selected) R.drawable.bg_k_chip_selected else R.drawable.bg_k_chip)
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }

    private fun renderEmptyState() {
        val binding = _binding ?: return
        val empty = viewModel.orders.value.isNullOrEmpty() &&
            viewModel.isRefreshing.value != true && viewModel.isLoadingMore.value != true
        binding.emptyStateContainer.visibility = if (empty) View.VISIBLE else View.GONE
        if (!empty) return

        val searchText = viewModel.searchQuery
        val hasSearch = searchText.isNotBlank()
        val statusName = orderStatusOptions.firstOrNull { it.first == viewModel.statusFilter }?.second
        val hasStatus = statusName != null
        binding.iconEmptyState.setImageResource(if (hasSearch) R.drawable.ic_k_ord_search else R.drawable.ic_k_ord_filter)
        binding.textEmpty.text = when {
            hasSearch && hasStatus -> getString(R.string.orders_empty_search_status, searchText, statusName)
            hasSearch -> getString(R.string.orders_empty_search, searchText)
            hasStatus -> getString(R.string.orders_empty_status, statusName)
            else -> getString(R.string.orders_empty_none)
        }
        binding.buttonReset.visibility = if (hasSearch || hasStatus) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
        binding.editSearch.clearFocus()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun openOrder(order: OnBehalfOrder) {
        findNavController().navigate(
            R.id.action_orders_to_detail,
            bundleOf(
                "orderId" to order.orderId,
                "incrementId" to order.incrementId,
                "customerId" to order.customerId,
                "customerName" to order.customerName,
                "grandTotal" to order.grandTotal,
                "status" to order.status,
                "createdAt" to order.createdAt
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchWatcher?.let { _binding?.editSearch?.removeTextChangedListener(it) }
        searchWatcher = null
        _binding = null
    }
}
