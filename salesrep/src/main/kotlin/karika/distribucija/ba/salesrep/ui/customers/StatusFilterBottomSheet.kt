package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R

class StatusFilterBottomSheet(
    private val onSelected: (String?) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_status_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listOf(
            R.id.option_all,
            R.id.option_active,
            R.id.option_pending,
            R.id.option_rejected,
            R.id.option_revoked
        ).forEach { id ->
            view.findViewById<View>(id).setOnClickListener {
                onSelected(it.tag as? String)
                dismiss()
            }
        }
    }
}
