package karika.distribucija.ba.salesrep.ui.customers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R

class AddCustomerBottomSheet(
    private val onNewCustomer: () -> Unit,
    private val onInviteCustomer: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_add_customer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.option_new_customer).setOnClickListener {
            dismiss()
            onNewCustomer()
        }
        view.findViewById<View>(R.id.option_invite_customer).setOnClickListener {
            dismiss()
            onInviteCustomer()
        }
    }
}
