package karika.distribucija.ba.salesrep.ui.customers

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.util.applyWhiteSheetBackground

/** Mirrors composeApp's SalesCustomersView.kt status ModalBottomSheet + StatusSheetItem. */
class StatusFilterBottomSheet(
    private val selectedStatus: String?,
    private val onSelected: (String?) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_status_filter, container, false)

    override fun onStart() {
        super.onStart()
        applyWhiteSheetBackground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rows = listOf(
            R.id.option_all to (R.id.label_all to R.id.check_all),
            R.id.option_active to (R.id.label_active to R.id.check_active),
            R.id.option_pending to (R.id.label_pending to R.id.check_pending),
            R.id.option_rejected to (R.id.label_rejected to R.id.check_rejected),
            R.id.option_revoked to (R.id.label_revoked to R.id.check_revoked)
        )

        rows.forEach { (rowId, labelAndCheck) ->
            val (labelId, checkId) = labelAndCheck
            val row = view.findViewById<LinearLayout>(rowId)
            val label = view.findViewById<TextView>(labelId)
            val check = view.findViewById<ImageView>(checkId)
            val status = row.tag as? String
            val selected = status == selectedStatus

            row.setBackgroundResource(if (selected) R.drawable.bg_status_sheet_selected else 0)
            label.setTextColor(requireContext().getColor(if (selected) R.color.karika_blue else R.color.karika_gray2))
            label.setTypeface(label.typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
            check.visibility = if (selected) View.VISIBLE else View.GONE

            row.setOnClickListener {
                onSelected(status)
                dismiss()
            }
        }
    }
}
