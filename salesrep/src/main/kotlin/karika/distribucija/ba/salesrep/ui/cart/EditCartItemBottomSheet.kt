package karika.distribucija.ba.salesrep.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.BottomSheetEditCartItemBinding
import karika.distribucija.ba.salesrep.model.OnBehalfCartResponseItem

/** Mirrors composeApp's EditCartItemModal in SalesOrderReviewView.kt - qty is always editable,
 * discount is editable only if [canDiscount] (otherwise shown read-only if already set, matching
 * `if (canDiscount) ... else if ((item.discountPercent ?: 0) > 0) ...`). Confirm is disabled
 * unless qty is a positive number. */
class EditCartItemBottomSheet(
    private val item: OnBehalfCartResponseItem,
    private val canDiscount: Boolean,
    private val onConfirm: (newQty: Int, newDiscountPercent: Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditCartItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditCartItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textItemName.text = item.name
        binding.editQty.setText(item.qty.toString())

        val existingDiscount = item.discountPercent?.takeIf { it > 0 }
        binding.layoutDiscountField.visibility = if (canDiscount) View.VISIBLE else View.GONE
        binding.textDiscountReadonly.visibility = if (!canDiscount && existingDiscount != null) View.VISIBLE else View.GONE
        if (canDiscount) {
            binding.editDiscount.setText(existingDiscount?.toString().orEmpty())
        } else if (existingDiscount != null) {
            binding.textDiscountReadonly.text = getString(R.string.review_edit_item_discount_readonly_format, existingDiscount)
        }

        binding.editQty.addTextChangedListener(onTextChanged = { _, _, _, _ -> renderConfirmEnabled() })

        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonConfirm.setOnClickListener {
            val newQty = binding.editQty.text?.toString()?.toIntOrNull() ?: item.qty
            if (newQty <= 0) return@setOnClickListener
            val newDiscount = (binding.editDiscount.text?.toString()?.toIntOrNull() ?: 0).coerceIn(0, 100)
            onConfirm(newQty, newDiscount)
            dismiss()
        }

        renderConfirmEnabled()
    }

    private fun renderConfirmEnabled() {
        val qtyValid = (binding.editQty.text?.toString()?.toIntOrNull() ?: 0) > 0
        binding.buttonConfirm.isEnabled = qtyValid
        binding.buttonConfirm.setBackgroundResource(
            if (qtyValid) R.drawable.bg_button_pill_primary else R.drawable.bg_button_pill_disabled
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
