package karika.distribucija.ba.salesrep.ui.customers

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentCustomersListBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.session.CurrentUser
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding

class CustomersListFragment : Fragment() {

    private var _binding: FragmentCustomersListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomersViewModel by viewModels()
    private lateinit var adapter: CustomersAdapter
    private var searchWatcher: TextWatcher? = null

    /** Kept alongside the ViewModel's own filter state so the chip label and empty-state
     * message can be rendered without re-querying the ViewModel for display text. */
    private var selectedStatusValue: String? = "active"

    private val statusOptions = listOf(
        "active" to R.string.customers_filter_active,
        "pending" to R.string.customers_filter_pending,
        "rejected" to R.string.customers_filter_rejected,
        "revoked" to R.string.customers_filter_revoked
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CustomersAdapter(
            onClick = ::openCustomer,
            onOrderClick = ::openCatalog,
            onMessageClick = ::openMessage,
            onDiscountClick = ::openDiscount,
            onHistoryClick = { openHistory() },
            onShowReps = { reps ->
                RepsBottomSheet(reps).show(childFragmentManager, "reps")
            }
        )
        binding.recyclerCustomers.adapter = adapter
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCustomers.applyImeBottomPadding()
        binding.recyclerCustomers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (dy > 0 && lastVisible >= adapter.itemCount - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        binding.rowCustomerTabs.visibility = if (viewModel.canSeeAllCustomers) View.VISIBLE else View.GONE

        updateTabSelection(CustomersViewModel.Tab.ALL)
        updateStatusChip(selectedStatusValue)
        binding.tabAll.setOnClickListener {
            AnalyticsTracker.trackClick("customers", "tab_all")
            updateTabSelection(CustomersViewModel.Tab.ALL)
            viewModel.selectTab(CustomersViewModel.Tab.ALL)
            clearStatusFilterUi()
        }
        binding.tabMine.setOnClickListener {
            AnalyticsTracker.trackClick("customers", "tab_mine")
            updateTabSelection(CustomersViewModel.Tab.MINE)
            viewModel.selectTab(CustomersViewModel.Tab.MINE)
            clearStatusFilterUi()
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        searchWatcher = binding.editSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val query = text?.toString().orEmpty()
            binding.iconClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            viewModel.setSearch(query)
        })
        binding.iconClearSearch.setOnClickListener {
            binding.editSearch.setText("")
        }

        binding.chipStatusFilter.setOnClickListener {
            StatusFilterBottomSheet(selectedStatusValue) { status ->
                selectedStatusValue = status
                updateStatusChip(status)
                viewModel.setStatus(status)
            }.show(childFragmentManager, "status_filter")
        }

        binding.buttonAdd.setOnClickListener {
            AnalyticsTracker.trackClick("customers", "add_customer")
            AddCustomerBottomSheet(
                onNewCustomer = { findNavController().navigate(R.id.action_customers_to_new_customer) },
                onInviteCustomer = {
                    findNavController().navigate(
                        R.id.action_customers_to_invite,
                        bundleOf("prefillEmail" to "")
                    )
                }
            ).show(childFragmentManager, "add_customer")
        }

        binding.rowReset.setOnClickListener {
            val hasSearch = binding.editSearch.text?.toString().orEmpty().isNotBlank()
            if (hasSearch) binding.editSearch.setText("")
            if (selectedStatusValue != null) {
                selectedStatusValue = null
                updateStatusChip(null)
                viewModel.setStatus(null)
            }
        }

        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            adapter.submitList(customers)
            renderEmptyState(customers.isEmpty())
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loadingMore ->
            renderLoadMoreFooter(loadingMore)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun updateTabSelection(tab: CustomersViewModel.Tab) {
        val allSelected = tab == CustomersViewModel.Tab.ALL
        binding.tabAll.setBackgroundResource(if (allSelected) R.drawable.bg_tab_pill_selected else 0)
        binding.tabMine.setBackgroundResource(if (!allSelected) R.drawable.bg_tab_pill_selected else 0)
        binding.tabAll.setTextColor(requireContext().getColor(if (allSelected) R.color.karika_blue else R.color.karika_gray6))
        binding.tabMine.setTextColor(requireContext().getColor(if (!allSelected) R.color.karika_blue else R.color.karika_gray6))
    }

    /** ViewModel.selectTab() already resets its own search/status state and triggers the
     * reload itself - this only needs to reset the displayed text, so the search TextWatcher
     * is detached first to avoid it firing a redundant, debounced setSearch()/loadPage(). */
    private fun clearStatusFilterUi() {
        selectedStatusValue = "active"
        updateStatusChip("active")
        searchWatcher?.let { binding.editSearch.removeTextChangedListener(it) }
        binding.editSearch.setText("")
        binding.iconClearSearch.visibility = View.GONE
        searchWatcher?.let { binding.editSearch.addTextChangedListener(it) }
    }

    private fun updateStatusChip(status: String?) {
        val isFiltered = status != null
        binding.chipStatusFilter.setBackgroundResource(
            if (isFiltered) R.drawable.bg_chip_filter_active else R.drawable.bg_chip_filter_inactive
        )
        val color = requireContext().getColor(if (isFiltered) R.color.karika_white else R.color.karika_gray2)
        binding.textStatusFilter.setTextColor(color)
        binding.iconStatusFilter.imageTintList = ColorStateList.valueOf(color)
        binding.textStatusFilter.text = status?.let { value ->
            statusOptions.firstOrNull { it.first == value }?.second?.let { getString(it) }
        } ?: getString(R.string.customers_status_all_sheet)
    }

    private fun renderEmptyState(isEmpty: Boolean) {
        if (!isEmpty) {
            binding.emptyStateContainer.visibility = View.GONE
            return
        }
        binding.emptyStateContainer.visibility = View.VISIBLE

        val searchText = binding.editSearch.text?.toString().orEmpty()
        val hasSearch = searchText.isNotBlank()
        val hasStatus = selectedStatusValue != null
        val statusLabel = selectedStatusValue?.let { value ->
            statusOptions.firstOrNull { it.first == value }?.second?.let { getString(it) }
        }

        binding.iconEmptyState.setImageResource(if (hasSearch) R.drawable.ic_search else R.drawable.ic_filter_alt)
        binding.textEmptyMessage.text = when {
            hasSearch -> getString(R.string.customers_empty_search, searchText)
            hasStatus -> getString(R.string.customers_empty_status, statusLabel)
            else -> getString(R.string.customers_empty_default)
        }

        binding.rowReset.visibility = if (hasSearch || hasStatus) View.VISIBLE else View.GONE
        binding.textReset.text = when {
            hasSearch && hasStatus -> getString(R.string.customers_reset_both)
            hasSearch -> getString(R.string.customers_reset_search)
            else -> getString(R.string.customers_reset_filter)
        }
    }

    /** Pagination is driven entirely by the RecyclerView scroll listener above (near-bottom
     * triggers loadNextPage()) - this footer only ever shows the in-flight spinner, no manual
     * "load more" button. */
    private fun renderLoadMoreFooter(isLoadingMore: Boolean) {
        binding.progressLoadMore.visibility = if (isLoadingMore) View.VISIBLE else View.GONE
    }

    private fun openCustomer(customer: OperationalCustomer) {
        findNavController().navigate(
            R.id.action_customers_to_detail,
            bundleOf(
                "customerId" to customer.customerId,
                "company" to customer.company,
                "firstname" to customer.firstname,
                "lastname" to customer.lastname,
                "email" to customer.email,
                "partnershipStatus" to customer.partnershipStatus,
                "assignedNames" to customer.assignedEmployees.joinToString(", ") { it.displayName ?: "—" },
                "hasShippingAddress" to (customer.defaultShippingAddressId != null)
            )
        )
    }

    private fun openCatalog(customer: OperationalCustomer) {
        findNavController().navigate(
            R.id.action_customers_to_catalog,
            bundleOf(
                "customerId" to customer.customerId,
                "customerName" to (customer.company ?: customer.fullName),
                "customerCompany" to customer.company,
                "customerEmail" to customer.email,
                "partnershipStatus" to customer.partnershipStatus,
                "customerActive" to customer.isActive,
                "hasShippingAddress" to (customer.defaultShippingAddressId != null)
            )
        )
    }

    private fun openMessage(customer: OperationalCustomer) {
        findNavController().navigate(
            R.id.action_customers_to_new_message,
            bundleOf(
                "initialCustomerId" to customer.customerId,
                "initialCustomerCompany" to customer.company,
                "initialCustomerFirstname" to customer.firstname,
                "initialCustomerLastname" to customer.lastname
            )
        )
    }

    /** Mirrors composeApp's SalesCustomersComponent.openDiscount() - gated on the same
     * capability, with the same error message, before navigating. */
    private fun openDiscount(customer: OperationalCustomer) {
        if (CurrentUser.me?.capabilities?.canCreateDiscountFor != true) {
            Toast.makeText(requireContext(), R.string.customers_discount_no_permission, Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(
            R.id.action_customers_to_discount_form,
            bundleOf("customerId" to customer.customerId)
        )
    }

    /** Mirrors composeApp's SalesCustomersComponent.openOrderHistory() - switches to the Orders
     * root destination, replacing this screen in the back stack. */
    private fun openHistory() {
        findNavController().navigate(
            R.id.ordersListFragment,
            null,
            navOptions { popUpTo(R.id.ordersListFragment) { inclusive = true } }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchWatcher = null
        _binding = null
    }
}
