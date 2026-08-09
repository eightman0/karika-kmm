package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentDiscountFormBinding

/** Mirrors composeApp's ui/view/salesrep/customers/detail/SalesDiscountFormView.kt, simplified
 * to product-only search (see DiscountFormViewModel doc) - so the search dropdown only ever
 * shows the "ART." item row, never the "KAT." category row. */
class DiscountFormFragment : Fragment() {

    private var _binding: FragmentDiscountFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DiscountFormViewModel by viewModels()
    private lateinit var searchAdapter: ProductSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscountFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(
            if (viewModel.isEdit) R.string.discount_form_title_edit else R.string.discount_form_title_new
        )

        searchAdapter = ProductSearchAdapter { product ->
            viewModel.selectProduct(product)
            binding.editItemSearch.setText(viewModel.itemSearch.value)
            renderSearchField()
        }
        binding.recyclerSearchResults.adapter = searchAdapter
        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.editItemSearch.setText(viewModel.itemSearch.value)
        binding.editMinQty.setText(viewModel.minQty.value)
        binding.editDiscountPercent.setText(viewModel.discountPercent.value)
        renderSearchField()

        binding.editItemSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setItemSearch(text?.toString().orEmpty())
            renderSearchField()
        })
        binding.editMinQty.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setMinQty(text?.toString().orEmpty())
        })
        binding.editDiscountPercent.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setDiscountPercent(text?.toString().orEmpty())
        })
        binding.buttonClearItem.setOnClickListener {
            viewModel.clearItem()
            binding.editItemSearch.setText("")
            renderSearchField()
        }

        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSave.setOnClickListener { viewModel.save() }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.submitList(results)
            renderSearchField()
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.buttonSave.isEnabled = !saving
            binding.textSave.visibility = if (saving) View.GONE else View.VISIBLE
            binding.progressSave.visibility = if (saving) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                findNavController().popBackStack()
            }
        }
    }

    /** Mirrors the search Row's three visual states: selected (blue border, read-only, blue
     * text), dropdown open (top-rounded only, results attached below), or plain. */
    private fun renderSearchField() {
        val query = binding.editItemSearch.text?.toString().orEmpty()
        val hasSelectedItem = viewModel.hasSelectedItem
        val results = viewModel.searchResults.value.orEmpty()
        val showDropdown = results.isNotEmpty() && !hasSelectedItem

        binding.buttonClearItem.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
        binding.recyclerSearchResults.visibility = if (showDropdown) View.VISIBLE else View.GONE

        binding.editItemSearch.isFocusable = !hasSelectedItem
        binding.editItemSearch.isFocusableInTouchMode = !hasSelectedItem

        val fieldBg = when {
            hasSelectedItem -> R.drawable.bg_discount_search_field_selected
            showDropdown -> R.drawable.bg_discount_search_field_open
            else -> R.drawable.bg_discount_search_field
        }
        binding.searchFieldContainer.setBackgroundResource(fieldBg)

        val accentColor = requireContext().getColor(
            if (hasSelectedItem) R.color.karika_blue else R.color.karika_gray6
        )
        binding.iconItemSearch.setColorFilter(accentColor)
        binding.editItemSearch.setTextColor(
            requireContext().getColor(if (hasSelectedItem) R.color.karika_blue else R.color.karika_gray2)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
