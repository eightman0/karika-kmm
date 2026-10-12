package karika.distribucija.ba.salesrep.ui.customers

import karika.distribucija.ba.salesrep.util.showSnack
import android.app.AlertDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                showSnack(message)
            }
        }
    }

    private fun renderProfile() {
        val profileName = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName
        binding.textProfileName.text = profileName
        binding.textKInitials.text = profileName.trim().split(" ").take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifEmpty { "?" }
        // Status pill (soft background + dot), sentence case like the Compose KPill
        binding.textBadge.text = customer.detailBadgeLabel().lowercase().replaceFirstChar { it.uppercase() }
        val (badgeBgRes, badgeColorRes) = when (customer.partnershipStatus) {
            "active" -> R.drawable.bg_k_pill_green_soft to R.color.k_green
            "pending" -> R.drawable.bg_k_pill_amber_soft to R.color.k_amber
            "rejected" -> R.drawable.bg_k_pill_red_soft to R.color.k_red
            "revoked" -> R.drawable.bg_k_pill_field to R.color.k_muted
            else -> R.drawable.bg_k_pill_field to R.color.k_muted
        }
        binding.textBadge.setBackgroundResource(badgeBgRes)
        binding.textBadge.setTextColor(requireContext().getColor(badgeColorRes))
        binding.textBadge.compoundDrawableTintList =
            android.content.res.ColorStateList.valueOf(requireContext().getColor(badgeColorRes))

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
        if (!canCreateDiscount()) return
        findNavController().navigate(
            R.id.action_customer_detail_to_discount_form,
            bundleOf(
                "customerId" to viewModel.customerId,
                "customerName" to customerTitle()
            )
        )
    }

    private fun openEditDiscount(rule: DiscountRule) {
        if (!canCreateDiscount()) return
        findNavController().navigate(
            R.id.action_customer_detail_to_discount_form,
            bundleOf(
                "customerId" to viewModel.customerId,
                "customerName" to customerTitle(),
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

    private fun customerTitle(): String = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName

    /** Mirrors SalesCustomerDetailComponent.canCreateDiscount(): re-checks the capability on open. */
    private fun canCreateDiscount(): Boolean {
        if (CurrentUser.me?.capabilities?.canCreateDiscountFor == true) return true
        showSnack(getString(R.string.k_cust2_no_discount_permission))
        return false
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
