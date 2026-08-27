package karika.distribucija.ba.salesrep.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.BottomSheetForgotPasswordBinding
import karika.distribucija.ba.salesrep.util.applyWhiteSheetBackground
import karika.distribucija.ba.salesrep.util.isEmailFormatValid

/** Mirrors composeApp's ForgotPasswordSheet.kt. The email field always starts blank (Compose
 * doesn't prefill it from the login screen's own email field), and tapping "Potvrdi" dismisses
 * the sheet immediately and hands the email to the caller - matching Compose's
 * `showState.negate(); component.forgotPassword(email.value)` firing the request after the sheet
 * is already closing, rather than waiting for it in place. */
class ForgotPasswordBottomSheet(
    private val onSubmit: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        applyWhiteSheetBackground()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderConfirmEnabled()
        binding.editForgotEmail.addTextChangedListener(onTextChanged = { _, _, _, _ -> renderConfirmEnabled() })

        binding.buttonClose.setOnClickListener {
            hideKeyboard()
            dismiss()
        }

        binding.buttonConfirm.setOnClickListener {
            val email = binding.editForgotEmail.text?.toString().orEmpty()
            if (!email.isEmailFormatValid()) return@setOnClickListener
            hideKeyboard()
            dismiss()
            onSubmit(email)
        }
    }

    private fun renderConfirmEnabled() {
        val isValid = binding.editForgotEmail.text?.toString().orEmpty().isEmailFormatValid()
        binding.buttonConfirm.isEnabled = isValid
        binding.buttonConfirm.setBackgroundResource(
            if (isValid) R.drawable.bg_button_pill_primary else R.drawable.bg_button_pill_disabled
        )
    }

    private fun hideKeyboard() {
        requireContext().getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
