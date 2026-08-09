package karika.distribucija.ba.salesrep.ui.customers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerDetailBinding
import karika.distribucija.ba.salesrep.model.DiscountRule

class CustomerDetailFragment : Fragment() {

    private var _binding: FragmentCustomerDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerDetailViewModel by viewModels()
    private lateinit var adapter: DiscountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val company = args.getString("company")
        val firstname = args.getString("firstname")
        val lastname = args.getString("lastname")
        val status = args.getString("partnershipStatus").orEmpty()
        val isActive = status == "active"

        val displayName = company ?: listOfNotNull(firstname, lastname).joinToString(" ").ifEmpty { "—" }
        binding.textCompany.text = displayName
        (activity as? AppCompatActivity)?.supportActionBar?.title = displayName
        binding.textEmail.text = args.getString("email").orEmpty()
        binding.textAssigned.text = args.getString("assignedNames").orEmpty()
        binding.textStatus.text = statusLabel(status)
        binding.textStatus.background.setTint(statusColor(status))
        binding.buttonOrderForCustomer.visibility = if (isActive) View.VISIBLE else View.GONE

        adapter = DiscountsAdapter(
            onEdit = ::openEditDiscount,
            onDelete = ::confirmDelete
        )
        binding.recyclerDiscounts.adapter = adapter
        binding.recyclerDiscounts.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonNewDiscount.setOnClickListener { openNewDiscount() }
        binding.buttonOrderForCustomer.setOnClickListener {
            findNavController().navigate(
                R.id.action_customer_detail_to_catalog,
                bundleOf(
                    "customerId" to viewModel.customerId,
                    "customerName" to displayName,
                    "customerActive" to isActive,
                    "hasShippingAddress" to args.getBoolean("hasShippingAddress")
                )
            )
        }

        viewModel.discounts.observe(viewLifecycleOwner) { discounts ->
            adapter.submitList(discounts)
            binding.textEmpty.visibility = if (discounts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh after returning from the discount form (add/edit/delete) - the fragment view
        // is recreated on back-navigation but the ViewModel survives, so its LiveData would
        // otherwise still hold the pre-edit list.
        viewModel.loadDiscounts()
    }

    private fun openNewDiscount() {
        findNavController().navigate(
            R.id.action_customer_detail_to_discount_form,
            bundleOf("customerId" to viewModel.customerId)
        )
    }

    private fun openEditDiscount(rule: DiscountRule) {
        findNavController().navigate(
            R.id.action_customer_detail_to_discount_form,
            bundleOf(
                "customerId" to viewModel.customerId,
                "ruleId" to (rule.ruleId ?: 0L),
                "productId" to (rule.productId ?: 0L),
                "productName" to rule.productName,
                "categoryId" to (rule.categoryId ?: 0L),
                "categoryName" to rule.categoryName,
                "minQty" to (rule.minQty ?: 0f),
                "discountPercent" to rule.discountPercent,
                "isActive" to rule.isActive
            )
        )
    }

    private fun confirmDelete(rule: DiscountRule) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.discount_delete_title)
            .setMessage(R.string.discount_delete_message)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.deleteDiscount(rule) }
            .show()
    }

    private fun statusLabel(status: String): String = when (status) {
        "active" -> getString(R.string.customers_status_active)
        "pending" -> getString(R.string.customers_status_pending)
        "rejected" -> getString(R.string.customers_status_rejected)
        "revoked" -> getString(R.string.customers_status_revoked)
        else -> status
    }

    private fun statusColor(status: String): Int {
        val colorRes = when (status) {
            "active" -> R.color.status_approved
            "pending" -> R.color.status_pending
            "rejected" -> R.color.status_rejected
            "revoked" -> R.color.status_cancelled
            else -> R.color.status_default
        }
        return requireContext().getColor(colorRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
