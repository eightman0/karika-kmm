package karika.distribucija.ba.salesrep.ui.unavailable

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import karika.distribucija.ba.salesrep.MainActivity
import karika.distribucija.ba.salesrep.R

/** Shown instead of the app while `vendor_operations_enabled` is false or `me` failed - mirrors
 * composeApp's VendorOpsUnavailableView. */
class VendorOpsUnavailableFragment : Fragment(R.layout.fragment_vendor_ops_unavailable) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.button_retry).setOnClickListener {
            (activity as? MainActivity)?.retryMe()
        }
        view.findViewById<View>(R.id.button_logout).setOnClickListener {
            (activity as? MainActivity)?.logout()
        }
    }
}
