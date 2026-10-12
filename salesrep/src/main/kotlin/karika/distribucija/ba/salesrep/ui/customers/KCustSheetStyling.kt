package karika.distribucija.ba.salesrep.ui.customers

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R

/** Redesign sheet surface for the customers sheets: white with 28dp top corners (bg_k_sheet),
 * matching the Compose ModalBottomSheet shape in SalesCustomersView.kt. */
internal fun BottomSheetDialogFragment.applyKCustSheetBackground() {
    (dialog as? BottomSheetDialog)
        ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        ?.setBackgroundResource(R.drawable.bg_k_sheet)
}
