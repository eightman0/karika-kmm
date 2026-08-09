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

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonConfirm.setOnClickListener {
            viewModel.confirmOrder(binding.editNote.text?.toString().orEmpty())
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            adapter.submitList(cart?.items.orEmpty())
            binding.textSubtotal.text = cart?.subtotalString() ?: "0,00 KM"
            binding.textDiscount.text = cart?.discountString() ?: "0,00 KM"
            binding.textGrandTotal.text = cart?.grandTotalString() ?: "0,00 KM"
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
