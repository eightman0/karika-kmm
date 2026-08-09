package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentCustomersListBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer

class CustomersListFragment : Fragment() {

    private var _binding: FragmentCustomersListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomersViewModel by viewModels()
    private lateinit var adapter: CustomersAdapter

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
            onOrderClick = ::openCatalog
        )
        binding.recyclerCustomers.adapter = adapter
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerCustomers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (dy > 0 && lastVisible >= adapter.itemCount - 3) {
                    viewModel.loadNextPage()
                }
            }
        })

        binding.toggleTab.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.button_tab_all -> viewModel.selectTab(CustomersViewModel.Tab.ALL)
                R.id.button_tab_mine -> viewModel.selectTab(CustomersViewModel.Tab.MINE)
            }
        }
        binding.toggleTab.check(R.id.button_tab_all)

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        binding.editSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setSearch(text?.toString().orEmpty())
        })

        binding.buttonStatusFilter.setOnClickListener {
            StatusFilterBottomSheet { status -> viewModel.setStatus(status) }
                .show(childFragmentManager, "status_filter")
        }

        binding.buttonAdd.setOnClickListener {
            AddCustomerBottomSheet(
                onNewCustomer = { findNavController().navigate(R.id.action_customers_to_new_customer) },
                onInviteCustomer = { findNavController().navigate(R.id.action_customers_to_invite, bundleOf("prefillEmail" to "")) }
            ).show(childFragmentManager, "add_customer")
        }

        viewModel.customers.observe(viewLifecycleOwner) { customers ->
            adapter.submitList(customers)
            binding.textEmpty.visibility = if (customers.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
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
                "customerActive" to customer.isActive,
                "hasShippingAddress" to (customer.defaultShippingAddressId != null)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
