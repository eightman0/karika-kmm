package karika.distribucija.ba.salesrep.ui.cart

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
import karika.distribucija.ba.salesrep.databinding.FragmentOrderReviewBinding
import karika.distribucija.ba.salesrep.session.CartState
import karika.distribucija.ba.salesrep.util.karikaPriceFormat

class OrderReviewFragment : Fragment() {

    private var _binding: FragmentOrderReviewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderReviewViewModel by viewModels()
    private lateinit var adapter: ReviewItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customerName = requireArguments().getString("customerName").orEmpty()
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.review_title)

        adapter = ReviewItemsAdapter()
        binding.recyclerItems.adapter = adapter
        binding.recyclerItems.layoutManager = LinearLayoutManager(requireContext())

        binding.textWarning.apply {
            when (viewModel.ineligibleReason) {
                "no_capability" -> { text = getString(R.string.review_warning_no_capability); visibility = View.VISIBLE }
                "inactive" -> { text = getString(R.string.review_warning_inactive); visibility = View.VISIBLE }
                "no_shipping" -> { text = getString(R.string.review_warning_no_shipping); visibility = View.VISIBLE }
                else -> visibility = View.GONE
            }
        }
        binding.buttonConfirm.isEnabled = viewModel.isEligible

        renderCustomerInfo()

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonConfirm.setOnClickListener {
            viewModel.confirmOrder(binding.editNote.text?.toString().orEmpty())
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            adapter.submitList(cart?.items.orEmpty())
            binding.textItemsCount.text = (cart?.itemsCount ?: cart?.items?.size ?: 0).toString()
            binding.textTotalVpc.text = cart?.grandTotalString() ?: "0,00 KM"
            binding.textReviewSubtotal.text = cart?.subtotalString() ?: "0,00 KM"
            binding.textReviewPdv.text = karikaPriceFormat(cart?.totalTax ?: 0.0) + " KM"
            binding.textReviewDiscount.text = "-" + (cart?.discountString() ?: "0,00 KM")
            binding.textReviewTotalWithTax.text = karikaPriceFormat(cart?.totalWithTax ?: 0.0) + " KM"
            binding.textReviewCommission.text = karikaPriceFormat(cart?.fee ?: 0.0) + " KM"
        }

        viewModel.isPlacingOrder.observe(viewLifecycleOwner) { saving ->
            binding.buttonConfirm.isEnabled = viewModel.isEligible && !saving
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.shouldGoBack.observe(viewLifecycleOwner) { shouldGoBack ->
            if (shouldGoBack) findNavController().popBackStack()
        }

        viewModel.orderPlaced.observe(viewLifecycleOwner) { order ->
            if (order != null) {
                val message = if (order.status == "pending") {
                    "Narudžba ${order.incrementId} je poslana menadžeru na odobrenje."
                } else {
                    "Narudžba ${order.incrementId} uspješno kreirana!"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

                findNavController().navigate(
                    R.id.orderDetailFragment,
                    bundleOf(
                        "orderId" to order.orderId,
                        "incrementId" to order.incrementId,
                        "customerId" to viewModel.customerId,
                        "customerName" to customerName,
                        "grandTotal" to order.grandTotal.toFloat(),
                        "status" to order.status,
                        "createdAt" to null
                    ),
                    androidx.navigation.navOptions {
                        popUpTo(R.id.ordersListFragment) { inclusive = false }
                    }
                )
            }
        }
    }

    /** Mirrors the Compose Review screen's "Informacije o narudžbi" card: customer company/email,
     * a two-state (active/not-active) partnership badge, and the items count. */
    private fun renderCustomerInfo() {
        val customerName = requireArguments().getString("customerName").orEmpty()
        binding.textCustomerCompany.text = viewModel.customerCompany?.takeIf { it.isNotBlank() } ?: customerName
        binding.textCustomerEmail.text =
            viewModel.customerEmail?.takeIf { it.isNotBlank() } ?: getString(R.string.order_detail_dash)

        val badgeLabel = when (viewModel.partnershipStatus) {
            "active" -> R.string.review_badge_active
            "pending" -> R.string.review_badge_pending
            "rejected" -> R.string.review_badge_rejected
            "revoked" -> R.string.review_badge_revoked
            else -> R.string.review_badge_pending
        }
        binding.textPartnershipBadge.text = getString(badgeLabel)
        val (badgeBgRes, badgeColorRes) = if (viewModel.customerActive) {
            R.drawable.bg_review_badge_active to R.color.karika_green3
        } else {
            R.drawable.bg_review_badge_pending to R.color.karika_blue
        }
        binding.textPartnershipBadge.setBackgroundResource(badgeBgRes)
        binding.textPartnershipBadge.setTextColor(requireContext().getColor(badgeColorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
