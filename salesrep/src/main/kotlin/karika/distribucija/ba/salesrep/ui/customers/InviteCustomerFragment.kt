package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.databinding.FragmentInviteCustomerBinding

class InviteCustomerFragment : Fragment() {

    private var _binding: FragmentInviteCustomerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InviteCustomerViewModel by viewModels()
    private lateinit var searchAdapter: CustomerSearchAdapter

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

        searchAdapter = CustomerSearchAdapter { customer -> viewModel.selectCustomer(customer) }
        binding.recyclerSearchResults.adapter = searchAdapter
        binding.recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.editCustomerSearch.setText(viewModel.searchQuery.value)
        binding.editEmail.setText(viewModel.email.value)

        binding.editCustomerSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setSearchQuery(text?.toString().orEmpty())
        })
        binding.editEmail.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setEmail(text?.toString().orEmpty())
        })

        binding.buttonSend.setOnClickListener {
            viewModel.send(binding.editNote.text?.toString().orEmpty())
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            searchAdapter.submitList(results)
            binding.recyclerSearchResults.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.buttonSend.isEnabled = !saving
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
