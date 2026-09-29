package karika.distribucija.ba.salesrep.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import karika.distribucija.ba.salesrep.databinding.FragmentAnalyticsFiltersBinding
import karika.distribucija.ba.salesrep.ui.customers.PickerBottomSheet
import karika.distribucija.ba.salesrep.util.toDisplayDate

/** "Filteri" screen of the Analitika feature - a plain form over [AnalyticsFilterState]'s
 * LiveData fields, with no ViewModel of its own. Reached via a forward push from any Analitika
 * screen's filter bar; pressing back here (in-body row, toolbar/system back, or "Primijeni")
 * always calls [AnalyticsFilterState.apply] first so every other analytics screen's
 * `applyTrigger` observer refetches with the latest filters. */
class AnalyticsFiltersFragment : Fragment() {

    private var _binding: FragmentAnalyticsFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsFiltersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rowBack.setOnClickListener { applyAndPop() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { applyAndPop() }

        bindDateFields()
        bindPickerFields()
        bindTextFields()

        binding.buttonApply.setOnClickListener { applyAndPop() }
    }

    private fun bindDateFields() {
        binding.textDateFrom.text = AnalyticsFilterState.dateFromMillis.value?.toDisplayDate()
        binding.textDateTo.text = AnalyticsFilterState.dateToMillis.value?.toDisplayDate()

        binding.fieldDateFrom.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setSelection(AnalyticsFilterState.dateFromMillis.value)
                .build()
                .apply {
                    addOnPositiveButtonClickListener { selection ->
                        AnalyticsFilterState.dateFromMillis.value = selection
                        binding.textDateFrom.text = selection.toDisplayDate()
                    }
                }
                .show(childFragmentManager, "date_from_picker")
        }

        binding.fieldDateTo.setOnClickListener {
            MaterialDatePicker.Builder.datePicker()
                .setSelection(AnalyticsFilterState.dateToMillis.value)
                .build()
                .apply {
                    addOnPositiveButtonClickListener { selection ->
                        AnalyticsFilterState.dateToMillis.value = selection
                        binding.textDateTo.text = selection.toDisplayDate()
                    }
                }
                .show(childFragmentManager, "date_to_picker")
        }
    }

    private fun bindPickerFields() {
        binding.textGroupingValue.text = AnalyticsFilterState.grouping.value
        binding.fieldGrouping.setOnClickListener {
            PickerBottomSheet(
                "Grupisanje",
                AnalyticsFilterState.groupingOptions,
                AnalyticsFilterState.grouping.value
            ) { picked ->
                AnalyticsFilterState.grouping.value = picked
                binding.textGroupingValue.text = picked
            }.show(childFragmentManager, "grouping_picker")
        }

        binding.textComparisonValue.text = AnalyticsFilterState.comparison.value
        binding.fieldComparison.setOnClickListener {
            PickerBottomSheet(
                "Poređenje",
                AnalyticsFilterState.comparisonOptions,
                AnalyticsFilterState.comparison.value
            ) { picked ->
                AnalyticsFilterState.comparison.value = picked
                binding.textComparisonValue.text = picked
            }.show(childFragmentManager, "comparison_picker")
        }
    }

    private fun bindTextFields() {
        binding.editRep.setText(AnalyticsFilterState.repQuery.value)
        binding.editRep.addTextChangedListener { AnalyticsFilterState.repQuery.value = it.toString() }

        binding.editCustomer.setText(AnalyticsFilterState.customerQuery.value)
        binding.editCustomer.addTextChangedListener { AnalyticsFilterState.customerQuery.value = it.toString() }

        binding.editProduct.setText(AnalyticsFilterState.productQuery.value)
        binding.editProduct.addTextChangedListener { AnalyticsFilterState.productQuery.value = it.toString() }

        binding.editCategory.setText(AnalyticsFilterState.categoryQuery.value)
        binding.editCategory.addTextChangedListener { AnalyticsFilterState.categoryQuery.value = it.toString() }
    }

    private fun applyAndPop() {
        AnalyticsFilterState.apply()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
