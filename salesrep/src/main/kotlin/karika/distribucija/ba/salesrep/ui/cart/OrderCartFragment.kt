package karika.distribucija.ba.salesrep.ui.cart

import karika.distribucija.ba.salesrep.util.showSnack
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderCartBinding
import karika.distribucija.ba.salesrep.session.CartState

class OrderCartFragment : Fragment() {

    private var _binding: FragmentOrderCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderCartViewModel by viewModels()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val customerName = args.getString("customerName").orEmpty()
        val customerCompany = args.getString("customerCompany")
        // Company first, the person's name only when there is none - same as composeApp's
        // SalesOrderCartView/SalesOrderCatalogView header.
        val customerLabel = customerCompany?.takeIf { it.isNotBlank() } ?: customerName
        val customerEmail = args.getString("customerEmail")
        val partnershipStatus = args.getString("partnershipStatus").orEmpty()
        val customerActive = args.getBoolean("customerActive")
        val hasShippingAddress = args.getBoolean("hasShippingAddress")
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.cart_title) + ": $customerLabel"

        adapter = CartAdapter(
            lifecycleOwner = viewLifecycleOwner,
            canDiscount = viewModel.canCreateDiscountFor,
            onQtyChanged = { item, qty -> viewModel.updateQty(item, qty) },
            onDiscountChanged = { item, percent -> viewModel.updateDiscount(item, percent) },
            onRemove = { item -> viewModel.removeItem(item) }
        )
        val titleRow = CartTitleAdapter(onClear = {
            AnalyticsTracker.trackClick("cart", "clear_cart")
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.cart_clear_title)
                .setMessage(R.string.cart_clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.clearCart() }
                .show()
        })
        val customerHeader = CartCustomerHeaderAdapter(customerLabel, customerEmail)
        binding.recyclerCart.adapter = ConcatAdapter(titleRow, customerHeader, adapter)
        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonReview.setOnClickListener {
            if (CartState.cart.value?.isEmpty != false) return@setOnClickListener
            AnalyticsTracker.trackClick("cart", "review_order")
            findNavController().navigate(
                R.id.action_cart_to_review,
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

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) showSnack(message)
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            val items = cart?.items.orEmpty()
            adapter.submitList(items)
            titleRow.setCount(items.size)
            customerHeader.visible = items.isNotEmpty()
            binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.layoutSummary.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            binding.textSubtotal.text = cart?.subtotalString() ?: "0,00 KM"
            binding.textDiscount.text = "-" + (cart?.discountString() ?: "0,00 KM")
            binding.textGrandTotal.text = cart?.grandTotalString() ?: "0,00 KM"
            binding.buttonReview.setBackgroundResource(
                if (items.isNotEmpty()) R.drawable.bg_k_button_primary else R.drawable.bg_k_ord_button_disabled
            )
            binding.buttonReview.isEnabled = items.isNotEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
