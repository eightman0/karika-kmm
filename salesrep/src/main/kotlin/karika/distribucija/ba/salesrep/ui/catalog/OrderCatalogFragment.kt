package karika.distribucija.ba.salesrep.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentOrderCatalogBinding
import karika.distribucija.ba.salesrep.session.CartState

class OrderCatalogFragment : Fragment() {

    private var _binding: FragmentOrderCatalogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderCatalogViewModel by viewModels()
    private lateinit var adapter: ProductCatalogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val customerName = requireArguments().getString("customerName").orEmpty()
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.customers_order_for) + ": $customerName"

        adapter = ProductCatalogAdapter(
            getQty = { product -> viewModel.getCartQty(product) },
            onQtyChanged = { product, qty -> viewModel.changeQty(product, qty) }
        )
        binding.recyclerProducts.adapter = adapter
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerProducts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
                R.id.button_tab_all -> viewModel.selectTab(OrderCatalogViewModel.Tab.ALL)
                R.id.button_tab_previous -> viewModel.selectTab(OrderCatalogViewModel.Tab.PREVIOUSLY_ORDERED)
            }
        }
        binding.toggleTab.check(R.id.button_tab_all)

        binding.editSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setSearch(text?.toString().orEmpty())
        })

        binding.buttonCart.setOnClickListener {
            findNavController().navigate(
                R.id.action_catalog_to_cart,
                bundleOf(
                    "customerId" to viewModel.customerId,
                    "customerName" to customerName
                )
            )
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            binding.textEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        CartState.cart.observe(viewLifecycleOwner) { cart ->
            val count = cart?.itemsCount ?: 0
            binding.textCartBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.textCartBadge.text = count.toString()
            adapter.refreshQuantities()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
