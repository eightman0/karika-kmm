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

        searchAdapter = ProductSearchAdapter { product -> viewModel.selectProduct(product) }
        binding.recyclerSearchResults.adapter = searchAdapter
        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.editItemSearch.setText(viewModel.itemSearch.value)
        binding.editMinQty.setText(viewModel.minQty.value)
        binding.editDiscountPercent.setText(viewModel.discountPercent.value)

        binding.editItemSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setItemSearch(text?.toString().orEmpty())
        })
        binding.editMinQty.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setMinQty(text?.toString().orEmpty())
        })
        binding.editDiscountPercent.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setDiscountPercent(text?.toString().orEmpty())
        })
        binding.layoutItemSearch.setEndIconOnClickListener {
            viewModel.clearItem()
            binding.editItemSearch.setText("")
        }

        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSave.setOnClickListener { viewModel.save() }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.submitList(results)
            binding.recyclerSearchResults.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.buttonSave.isEnabled = !saving
            binding.buttonSave.text = if (saving) "" else getString(R.string.discount_form_save)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
