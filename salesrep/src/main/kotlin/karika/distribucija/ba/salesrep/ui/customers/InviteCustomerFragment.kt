package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentInviteCustomerBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer

/** Mirrors composeApp's ui/view/salesrep/customers/invite/SalesInviteCustomerView.kt. */
class InviteCustomerFragment : Fragment() {

    private var _binding: FragmentInviteCustomerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InviteCustomerViewModel by viewModels()

    /** Suppresses the search TextWatcher while we set the field's text programmatically
     * (after a selection), since composeApp's controlled TextField doesn't re-trigger a
     * search when its own state changes the displayed text. */
    private var suppressSearchWatcher = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInviteCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editCustomerSearch.setText(viewModel.searchQuery.value)

        binding.editCustomerSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            if (suppressSearchWatcher) return@addTextChangedListener
            val query = text?.toString().orEmpty()
            binding.iconClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            viewModel.setSearchQuery(query)
            renderResults()
        })
        binding.iconClearSearch.setOnClickListener {
            binding.editCustomerSearch.setText("")
            binding.resultsContainer.visibility = View.GONE
        }

        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSend.setOnClickListener {
            viewModel.send(binding.editNote.text?.toString().orEmpty())
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { renderResults() }
        viewModel.isSearching.observe(viewLifecycleOwner) { searching ->
            binding.progressSearch.visibility = if (searching) View.VISIBLE else View.GONE
            renderResults()
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.buttonSend.isEnabled = !saving
            binding.textSend.visibility = if (saving) View.GONE else View.VISIBLE
            binding.progressSend.visibility = if (saving) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.sent.observe(viewLifecycleOwner) { sent ->
            if (sent) findNavController().popBackStack()
        }
    }

    /** Mirrors the ExposedDropdownMenu's three states: too-short query, no results, or a list. */
    private fun renderResults() {
        val query = binding.editCustomerSearch.text?.toString().orEmpty()
        val results = viewModel.searchResults.value.orEmpty()

        binding.resultsContainer.removeAllViews()

        if (query.isEmpty()) {
            binding.resultsContainer.visibility = View.GONE
            return
        }

        binding.resultsContainer.visibility = View.VISIBLE
        if (query.length < 3) {
            binding.resultsContainer.addView(buildHintRow(R.string.invite_search_min_chars))
        } else if (results.isEmpty()) {
            binding.resultsContainer.addView(buildHintRow(R.string.invite_search_no_results))
        } else {
            results.forEach { customer -> binding.resultsContainer.addView(buildResultRow(customer)) }
        }
    }

    private fun buildHintRow(textRes: Int): View {
        val row = layoutInflater.inflate(R.layout.item_customer_search, binding.resultsContainer, false) as TextView
        row.setText(textRes)
        row.setTextColor(requireContext().getColor(R.color.karika_gray8))
        row.isClickable = false
        row.isFocusable = false
        return row
    }

    private fun buildResultRow(customer: OperationalCustomer): View {
        val row = layoutInflater.inflate(R.layout.item_customer_search, binding.resultsContainer, false) as TextView
        row.text = customer.company ?: customer.fullName
        row.setTextColor(requireContext().getColor(R.color.karika_gray2))
        row.setOnClickListener {
            viewModel.selectCustomer(customer)
            suppressSearchWatcher = true
            binding.editCustomerSearch.setText(viewModel.searchQuery.value)
            suppressSearchWatcher = false
            binding.iconClearSearch.visibility = View.VISIBLE
            binding.resultsContainer.visibility = View.GONE
        }
        return row
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
