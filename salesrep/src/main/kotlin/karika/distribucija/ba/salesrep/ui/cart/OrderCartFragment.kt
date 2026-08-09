package karika.distribucija.ba.salesrep.ui.cart

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
        val customerActive = args.getBoolean("customerActive")
        val hasShippingAddress = args.getBoolean("hasShippingAddress")
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.cart_title) + ": $customerName"

        adapter = CartAdapter(
            canDiscount = viewModel.canCreateDiscountFor,
            onQtyChanged = { item, qty -> viewModel.updateQty(item, qty) },
            onDiscountChanged = { item, percent -> viewModel.updateDiscount(item, percent) },
            onRemove = { item -> viewModel.removeItem(item) }
        )
        binding.recyclerCart.adapter = adapter
        binding.recyclerCart.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonClear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.cart_clear_title)
                .setMessage(R.string.cart_clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.clearCart() }
                .show()
        }

        binding.buttonReview.setOnClickListener {
            findNavController().navigate(
                R.id.action_cart_to_review,
                bundleOf(
                    "customerId" to viewModel.customerId,
                    "customerName" to customerName,
                    "customerActive" to customerActive,
                    "hasShippingAddress" to hasShippingAddress
                )
            )
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            adapter.submitList(cart?.items.orEmpty())
            binding.textEmpty.visibility = if (cart?.items.isNullOrEmpty()) View.VISIBLE else View.GONE
            binding.layoutSummary.visibility = if (cart?.items.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.textSubtotal.text = cart?.subtotalString() ?: "0,00 KM"
            binding.textDiscount.text = cart?.discountString() ?: "0,00 KM"
            binding.textGrandTotal.text = cart?.grandTotalString() ?: "0,00 KM"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
