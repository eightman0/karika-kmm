package karika.distribucija.ba.salesrep.ui.customers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentNewCustomerBinding

class NewCustomerFragment : Fragment() {

    private var _binding: FragmentNewCustomerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewCustomerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindTextField(binding.editCompany) { viewModel.company = it }
        bindTextField(binding.editIdNumber) { viewModel.idNumber = it }
        bindTextField(binding.editVatNumber) { viewModel.vatNumber = it }
        bindTextField(binding.editStreet) { viewModel.street = it }
        bindTextField(binding.editPostcode) { viewModel.postcode = it }
        bindTextField(binding.editEmployeeCount) { viewModel.employeeCount = it }
        bindTextField(binding.editFirstname) { viewModel.firstname = it }
        bindTextField(binding.editLastname) { viewModel.lastname = it }
        bindTextField(binding.editPhone) { viewModel.phone = it }
        bindTextField(binding.editEmail) { viewModel.email = it }

        binding.dropdownEntity.setAdapter(dropdownAdapter(viewModel.entityOptions))
        binding.dropdownEntity.setOnItemClickListener { _, _, position, _ ->
            val value = viewModel.entityOptions[position]
            viewModel.onEntitySelected(value)
            binding.dropdownCanton.setText("", false)
            binding.dropdownCity.setText("", false)
            binding.layoutCanton.hint = viewModel.cantonLabel()
        }

        binding.dropdownStoreSize.setAdapter(dropdownAdapter(viewModel.storeSizeOptions))
        binding.dropdownStoreSize.setOnItemClickListener { _, _, position, _ ->
            viewModel.storeSize = viewModel.storeSizeOptions[position]
        }

        binding.dropdownStoreType.setAdapter(dropdownAdapter(viewModel.storeTypeOptions))
        binding.dropdownStoreType.setOnItemClickListener { _, _, position, _ ->
            viewModel.storeType = viewModel.storeTypeOptions[position]
        }

        binding.dropdownCanton.setOnItemClickListener { _, _, position, _ ->
            val options = viewModel.cantonOptions.value.orEmpty()
            if (position < options.size) viewModel.onCantonSelected(options[position])
        }

        binding.dropdownCity.setOnItemClickListener { _, _, position, _ ->
            val options = viewModel.cityOptions.value.orEmpty()
            if (position < options.size) viewModel.onCitySelected(options[position])
        }

        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSave.setOnClickListener { viewModel.save() }

        viewModel.cantonOptions.observe(viewLifecycleOwner) { options ->
            binding.layoutCanton.visibility = if (options.isEmpty()) View.GONE else View.VISIBLE
            binding.dropdownCanton.setAdapter(dropdownAdapter(options))
        }

        viewModel.cityOptions.observe(viewLifecycleOwner) { options ->
            binding.layoutCity.visibility = if (options.isEmpty()) View.GONE else View.VISIBLE
            binding.dropdownCity.setAdapter(dropdownAdapter(options))
        }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.buttonSave.isEnabled = !saving
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.saved.observe(viewLifecycleOwner) { saved ->
            if (saved) findNavController().popBackStack()
        }

        viewModel.showInviteDialog.observe(viewLifecycleOwner) { show ->
            if (show) showDuplicateEmailDialog()
        }
    }

    private fun bindTextField(editText: android.widget.EditText, onChanged: (String) -> Unit) {
        editText.addTextChangedListener(onTextChanged = { text, _, _, _ -> onChanged(text?.toString().orEmpty()) })
    }

    private fun dropdownAdapter(items: List<String>) =
        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)

    private fun showDuplicateEmailDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_customer_duplicate_title)
            .setMessage(R.string.new_customer_duplicate_message)
            .setNegativeButton(R.string.action_cancel) { _, _ -> viewModel.dismissInviteDialog() }
            .setPositiveButton(R.string.new_customer_duplicate_invite) { _, _ ->
                viewModel.dismissInviteDialog()
                findNavController().navigate(
                    R.id.action_new_customer_to_invite,
                    bundleOf("prefillEmail" to viewModel.email.trim())
                )
            }
            .setOnDismissListener { viewModel.dismissInviteDialog() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
