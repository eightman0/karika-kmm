package karika.distribucija.ba.salesrep.ui.customers

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentNewCustomerBinding

/** Mirrors composeApp's ui/view/salesrep/customers/newcustomer/SalesNewCustomerView.kt. */
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

        binding.pickerEntity.setOnClickListener {
            PickerBottomSheet(
                title = "Entitet",
                options = viewModel.entityOptions,
                selected = viewModel.entity
            ) { value ->
                viewModel.onEntitySelected(value)
                binding.textEntityValue.text = value
                binding.textEntityValue.setTextColor(requireContext().getColor(R.color.karika_gray2))
                renderCantonCity()
            }.show(childFragmentManager, "picker_entity")
        }

        binding.pickerStoreSize.setOnClickListener {
            PickerBottomSheet(
                title = "Veličina objekta",
                options = viewModel.storeSizeOptions,
                selected = viewModel.storeSize
            ) { value ->
                viewModel.storeSize = value
                binding.textStoreSizeValue.text = value
                binding.textStoreSizeValue.setTextColor(requireContext().getColor(R.color.karika_gray2))
            }.show(childFragmentManager, "picker_store_size")
        }

        binding.pickerStoreType.setOnClickListener {
            PickerBottomSheet(
                title = "Tip objekta",
                options = viewModel.storeTypeOptions,
                selected = viewModel.storeType
            ) { value ->
                viewModel.storeType = value
                binding.textStoreTypeValue.text = value
                binding.textStoreTypeValue.setTextColor(requireContext().getColor(R.color.karika_gray2))
            }.show(childFragmentManager, "picker_store_type")
        }

        binding.pickerCanton.setOnClickListener {
            val options = viewModel.cantonOptions.value.orEmpty()
            val isFBiH = viewModel.entity == "Federacija"
            PickerBottomSheet(
                title = if (isFBiH) "Kanton" else "Općina",
                options = options,
                selected = viewModel.canton
            ) { value ->
                viewModel.onCantonSelected(value)
                binding.textCantonValue.text = value
                binding.textCantonValue.setTextColor(requireContext().getColor(R.color.karika_gray2))
                renderCity()
            }.show(childFragmentManager, "picker_canton")
        }

        binding.pickerCity.setOnClickListener {
            val options = viewModel.cityOptions.value.orEmpty()
            PickerBottomSheet(
                title = "Grad",
                options = options,
                selected = viewModel.city
            ) { value ->
                viewModel.onCitySelected(value)
                binding.textCityValue.text = value
                binding.textCityValue.setTextColor(requireContext().getColor(R.color.karika_gray2))
            }.show(childFragmentManager, "picker_city")
        }

        binding.buttonCancel.setOnClickListener { findNavController().popBackStack() }
        binding.buttonSave.setOnClickListener { viewModel.save() }

        viewModel.cantonOptions.observe(viewLifecycleOwner) { renderCantonCity() }
        viewModel.cityOptions.observe(viewLifecycleOwner) { renderCity() }

        viewModel.isSaving.observe(viewLifecycleOwner) { saving ->
            binding.buttonSave.isEnabled = !saving
            binding.buttonSave.setBackgroundResource(
                if (saving) R.drawable.bg_button_filled_disabled else R.drawable.bg_fab_order_customer
            )
            binding.textSave.visibility = if (saving) View.GONE else View.VISIBLE
            binding.progressSave.visibility = if (saving) View.VISIBLE else View.GONE
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

    /** Shows the Kanton/Općina picker once an entity requiring it is selected, and the Brčko
     * read-only city chip once "Distrikt Brčko" is selected. Mirrors SalesNewCustomerView.kt's
     * showCantonPicker/isBrcko derived flags. */
    private fun renderCantonCity() {
        val entity = viewModel.entity
        val isBrcko = entity == "Distrikt Brčko"
        val isFBiH = entity == "Federacija"
        val cantonOptions = viewModel.cantonOptions.value.orEmpty()
        val showCantonPicker = (isFBiH || entity == "Republika Srpska") && cantonOptions.isNotEmpty()

        binding.labelCanton.text = viewModel.cantonLabel()
        binding.layoutCantonPicker.visibility = if (showCantonPicker) View.VISIBLE else View.GONE
        binding.layoutCityReadonly.visibility = if (isBrcko) View.VISIBLE else View.GONE
        binding.textCityReadonly.text = viewModel.city.orEmpty()

        renderCity()
    }

    private fun renderCity() {
        val isFBiH = viewModel.entity == "Federacija"
        val cityOptions = viewModel.cityOptions.value.orEmpty()
        binding.layoutCityPicker.visibility = if (isFBiH && cityOptions.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindTextField(editText: android.widget.EditText, onChanged: (String) -> Unit) {
        editText.addTextChangedListener(onTextChanged = { text, _, _, _ -> onChanged(text?.toString().orEmpty()) })
    }

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
