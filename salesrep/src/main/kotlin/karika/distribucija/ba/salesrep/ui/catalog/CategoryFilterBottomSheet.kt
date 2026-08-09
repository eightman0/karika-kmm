package karika.distribucija.ba.salesrep.ui.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.BottomSheetCategoryFilterBinding
import karika.distribucija.ba.salesrep.model.Category

/** Mirrors composeApp's SalesOrderCatalogView.kt CategorySheet: hierarchical drill-down through
 * up to 3 levels with a back button and breadcrumb-style title, plus a flat deep-search across
 * all 3 levels. Selecting a category (or the "Sve kategorije"/"Svi artikli: X" header row) always
 * dismisses the sheet, matching Compose's onSelect-then-hide behavior. */
class CategoryFilterBottomSheet(
    private val allCategories: List<Category>,
    private val selectedCategory: Category?,
    initialNavStack: List<Category> = emptyList(),
    private val onSelect: (Category?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCategoryFilterBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CategoryRowAdapter

    private val navStack = initialNavStack.toMutableList()
    private var searchText = ""

    private val allFlatCategories: List<FlatCategory> by lazy { flattenCategoriesToDepth(allCategories) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCategoryFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val bottomSheet = (dialog as? BottomSheetDialog)
            ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        bottomSheet.layoutParams.height = (resources.displayMetrics.heightPixels * 0.9).toInt()
        bottomSheet.requestLayout()
        BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CategoryRowAdapter { row ->
            when {
                row.breadcrumb != null -> {
                    onSelect(row.category)
                    dismiss()
                }
                row.hasChildren -> {
                    navStack.add(row.category)
                    render()
                }
                else -> {
                    onSelect(row.category)
                    dismiss()
                }
            }
        }
        binding.recyclerCategories.adapter = adapter
        binding.recyclerCategories.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonCategoryBack.setOnClickListener {
            if (navStack.isNotEmpty()) {
                navStack.removeAt(navStack.lastIndex)
                render()
            }
        }
        binding.buttonCategoryClose.setOnClickListener { dismiss() }

        binding.editCategorySearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            searchText = text?.toString().orEmpty()
            render()
        })
        binding.buttonClearCategorySearch.setOnClickListener { binding.editCategorySearch.setText("") }

        binding.rowCategoryHeader.setOnClickListener {
            onSelect(navStack.lastOrNull())
            dismiss()
        }

        render()
    }

    private fun render() {
        val isSearching = searchText.isNotBlank()

        binding.buttonCategoryBack.visibility = if (navStack.isNotEmpty() && !isSearching) View.VISIBLE else View.GONE
        binding.buttonClearCategorySearch.visibility = if (searchText.isNotEmpty()) View.VISIBLE else View.GONE
        binding.textCategorySheetTitle.text = when {
            isSearching -> getString(R.string.category_sheet_search_title)
            navStack.isEmpty() -> getString(R.string.category_sheet_title_root)
            else -> navStack.last().name
        }
        binding.rowCategoryHeader.visibility = if (isSearching) View.GONE else View.VISIBLE

        if (isSearching) {
            val results = allFlatCategories.filter { it.category.name.contains(searchText, ignoreCase = true) }
            binding.textCategorySearchEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
            binding.textCategorySearchEmpty.text = getString(R.string.category_sheet_search_empty_format, searchText)
            binding.recyclerCategories.visibility = if (results.isEmpty()) View.GONE else View.VISIBLE
            adapter.submitList(results.map { flat ->
                CategoryRow(
                    category = flat.category,
                    breadcrumb = flat.ancestors.takeIf { it.isNotEmpty() }?.joinToString(" › ") { it.name },
                    hasChildren = false,
                    isSelected = selectedCategory?.id == flat.category.id
                )
            })
        } else {
            binding.textCategorySearchEmpty.visibility = View.GONE
            binding.recyclerCategories.visibility = View.VISIBLE

            val parentCategory = navStack.lastOrNull()
            val isParentSelected = if (parentCategory != null) {
                selectedCategory?.id == parentCategory.id
            } else {
                selectedCategory == null
            }
            binding.textCategoryHeaderLabel.text = if (parentCategory != null) {
                getString(R.string.category_sheet_all_in_format, parentCategory.name)
            } else {
                getString(R.string.category_sheet_all_root)
            }
            binding.rowCategoryHeader.setBackgroundResource(
                if (isParentSelected) R.drawable.bg_category_row_selected else R.drawable.bg_category_header_row_unselected
            )
            binding.textCategoryHeaderLabel.setTextColor(
                requireContext().getColor(if (isParentSelected) R.color.karika_blue else R.color.karika_gray2)
            )

            val currentCategories = if (navStack.isEmpty()) allCategories else navStack.last().childrenData
            adapter.submitList(currentCategories.map { category ->
                CategoryRow(
                    category = category,
                    breadcrumb = null,
                    hasChildren = category.childrenData.isNotEmpty(),
                    isSelected = selectedCategory?.id == category.id
                )
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
