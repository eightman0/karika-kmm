package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.model.AssignedEmployeeSummary

/** "Ada Lovelace" -> "AL", "Ada" -> "A". Mirrors SalesCustomersView.kt's private initials(). */
private fun String?.initials(): String =
    this?.trim()?.split(" ")?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("") ?: "?"

/** Mirrors composeApp's SalesCustomersView.kt "Komercijalisti" ModalBottomSheet. */
class RepsBottomSheet(
    private val reps: List<AssignedEmployeeSummary>
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_reps, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button_close).setOnClickListener { dismiss() }

        val container = view.findViewById<LinearLayout>(R.id.reps_list_container)
        reps.forEachIndexed { idx, employee ->
            val row = layoutInflater.inflate(R.layout.item_rep_row, container, false)
            row.findViewById<TextView>(R.id.text_avatar).apply {
                text = employee.displayName.initials()
                setBackgroundResource(
                    if (idx % 2 == 0) R.drawable.bg_avatar_plain_blue else R.drawable.bg_avatar_plain_secondary
                )
            }
            row.findViewById<TextView>(R.id.text_name).text = employee.displayName ?: "-"
            container.addView(row)
        }
    }
}
