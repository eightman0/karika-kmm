package karika.distribucija.ba.salesrep.ui.customers

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.util.applyWhiteSheetBackground

/** Mirrors composeApp's SalesNewCustomerView.kt SimplePickerSheet. */
class PickerBottomSheet(
    private val title: String,
    private val options: List<String>,
    private val selected: String?,
    private val onSelect: (String) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_picker, container, false)

    override fun onStart() {
        super.onStart()
        applyWhiteSheetBackground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.text_picker_title).text = title
        val container = view.findViewById<LinearLayout>(R.id.picker_options_container)

        options.forEachIndexed { index, option ->
            if (index > 0) {
                container.addView(View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)).apply {
                        marginStart = dp(20)
                        marginEnd = dp(20)
                    }
                    setBackgroundColor(requireContext().getColor(R.color.karika_gray9))
                })
            }

            val row = layoutInflater.inflate(R.layout.item_picker_row, container, false)
            val isSelected = option == selected
            row.findViewById<TextView>(R.id.text_option).apply {
                text = option
                setTextColor(requireContext().getColor(if (isSelected) R.color.karika_blue else R.color.karika_gray2))
                setTypeface(typeface, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
            }
            row.findViewById<View>(R.id.check_option).visibility = if (isSelected) View.VISIBLE else View.GONE
            row.setOnClickListener {
                onSelect(option)
                dismiss()
            }
            container.addView(row)
        }
    }

    private fun dp(value: Int): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        resources.displayMetrics
    ).toInt()
}
