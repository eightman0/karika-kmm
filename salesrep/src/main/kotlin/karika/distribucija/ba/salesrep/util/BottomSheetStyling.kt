package karika.distribucija.ba.salesrep.util

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R

/** Material3's default bottom sheet style paints a tonal surface color, not pure white - call
 * from onStart() so the sheet matches compose's ModalBottomSheet(containerColor = KarikaColors.White). */
fun BottomSheetDialogFragment.applyWhiteSheetBackground() {
    (dialog as? BottomSheetDialog)
        ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        ?.setBackgroundResource(R.drawable.bg_bottom_sheet_white)
}
