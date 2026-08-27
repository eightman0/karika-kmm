package karika.distribucija.ba.salesrep.ui.customers

import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
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
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerDetailBinding
import karika.distribucija.ba.salesrep.model.DiscountRule
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.session.CurrentUser

/** Mirrors composeApp's ui/view/salesrep/customers/detail/SalesCustomerDetailView.kt. */
class CustomerDetailFragment : Fragment() {

    private var _binding: FragmentCustomerDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerDetailViewModel by viewModels()
    private lateinit var adapter: DiscountsAdapter
    private lateinit var customer: OperationalCustomer
    private var assignedNames: String = ""

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
        customer = OperationalCustomer(
            customerId = viewModel.customerId,
            email = args.getString("email"),
            firstname = args.getString("firstname"),
            lastname = args.getString("lastname"),
            company = args.getString("company"),
            partnershipId = 0L,
            partnershipStatus = args.getString("partnershipStatus").orEmpty()
        )
        assignedNames = args.getString("assignedNames").orEmpty()

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName
        renderProfile()

        val canCreateDiscountFor = CurrentUser.me?.capabilities?.canCreateDiscountFor ?: false

        adapter = DiscountsAdapter(
            canEdit = canCreateDiscountFor,
            onEdit = ::openEditDiscount,
            onDelete = ::confirmDelete
        )
        binding.recyclerDiscounts.adapter = adapter
        binding.recyclerDiscounts.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonNewDiscount.visibility = if (canCreateDiscountFor) View.VISIBLE else View.GONE
        binding.buttonNewDiscount.setOnClickListener {
            AnalyticsTracker.trackClick("customer_detail", "new_discount")
            openNewDiscount()
        }
        binding.buttonOrderForCustomer.visibility = if (customer.isActive) View.VISIBLE else View.GONE
        binding.buttonOrderForCustomer.setOnClickListener {
            AnalyticsTracker.trackClick("customer_detail", "order_for_customer")
            findNavController().navigate(
                R.id.action_customer_detail_to_catalog,
                bundleOf(
                    "customerId" to viewModel.customerId,
                    "customerName" to customer.fullName,
                    "customerCompany" to customer.company,
                    "customerEmail" to customer.email,
                    "partnershipStatus" to customer.partnershipStatus,
                    "customerActive" to customer.isActive,
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

    private fun renderProfile() {
        binding.textProfileName.text = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName
        binding.textBadge.text = customer.detailBadgeLabel()
        val (badgeBgRes, badgeColorRes) = when (customer.partnershipStatus) {
            "active" -> R.drawable.bg_detail_badge_active to R.color.karika_green3
            "pending" -> R.drawable.bg_detail_badge_pending to R.color.karika_blue
            "rejected" -> R.drawable.bg_detail_badge_rejected to R.color.karika_error
            "revoked" -> R.drawable.bg_detail_badge_revoked to R.color.karika_gray6
            else -> R.drawable.bg_detail_badge_revoked to R.color.karika_gray6
        }
        binding.textBadge.setBackgroundResource(badgeBgRes)
        binding.textBadge.setTextColor(requireContext().getColor(badgeColorRes))

        val hasEmail = !customer.email.isNullOrBlank()
        binding.rowEmail.visibility = if (hasEmail) View.VISIBLE else View.GONE
        binding.spacerEmail.visibility = if (hasEmail) View.VISIBLE else View.GONE
        binding.textEmail.text = customer.email.orEmpty()

        binding.textFullname.text = customer.fullName

        binding.rowReps.visibility = if (assignedNames.isNotBlank()) View.VISIBLE else View.GONE
        binding.textReps.text = assignedNames
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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_delete, null)
        dialogView.findViewById<android.widget.TextView>(R.id.text_dialog_title).setText(R.string.discount_delete_title)
        dialogView.findViewById<android.widget.TextView>(R.id.text_dialog_message).setText(R.string.discount_delete_message)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<View>(R.id.button_dialog_cancel).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<View>(R.id.button_dialog_confirm).setOnClickListener {
            viewModel.deleteDiscount(rule)
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
