package karika.distribucija.ba.salesrep.ui.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.logging.AnalyticsTracker
import karika.distribucija.ba.logging.KioskIpc
import karika.distribucija.ba.salesrep.BuildConfig
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.SalesRepApp
import karika.distribucija.ba.salesrep.databinding.FragmentLoginBinding
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.notifications.PushTokenRegistrar
import karika.distribucija.ba.salesrep.util.isEmailFormatValid
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = (requireActivity().application as SalesRepApp).sessionManager
        val rememberedEmail = sessionManager.rememberedEmail()
        val rememberedPassword = sessionManager.rememberedPassword()
        binding.editEmail.setText(rememberedEmail)
        binding.editPassword.setText(rememberedPassword)
        binding.switchRememberMe.isChecked = rememberedPassword.isNotEmpty()

        updateFormValid()

        binding.textAppVersion.text = "v${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"

        // No account can log in without a network, so this is the one screen where getting stuck
        // offline (e.g. the kiosk moved to a location with different WiFi) has no other way out.
        // The kiosk (Device Owner) has "com.android.settings" allowlisted in lock task, so this
        // opens on top of the locked session instead of needing to leave it.
        binding.buttonWifiSettings.setOnClickListener {
            AnalyticsTracker.trackClick("login", "wifi_settings")
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        // The gradient/background image (this fragment's root FrameLayout) is left alone so it
        // bleeds edge-to-edge under the status bar; only the actual form content is pushed down
        // clear of it.
        val contentInitialTop = binding.contentContainer.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.contentContainer) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = contentInitialTop + statusBars.top)
            insets
        }

        binding.editEmail.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val email = text?.toString().orEmpty()
            val isInvalid = email.isNotEmpty() && !email.isEmailFormatValid()
            binding.textEmailError.visibility = if (isInvalid) {
                binding.textEmailError.text = getString(R.string.login_email_invalid)
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.editEmail.setBackgroundResource(
                if (isInvalid) R.drawable.bg_field_border_error else R.drawable.bg_field_border
            )
            updateFormValid()
        })
        binding.editPassword.addTextChangedListener(onTextChanged = { _, _, _, _ -> updateFormValid() })

        binding.textForgotPassword.setOnClickListener {
            AnalyticsTracker.trackClick("login", "forgot_password")
            ForgotPasswordBottomSheet { email -> forgotPasswordViewModel.submit(email) }
                .show(parentFragmentManager, "ForgotPasswordBottomSheet")
        }

        forgotPasswordViewModel.state.observe(viewLifecycleOwner) { result ->
            val message = when (result) {
                is ResultState.Success -> result.data
                is ResultState.Error -> result.message
                else -> null
            } ?: return@observe
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }

        binding.buttonLogin.setOnClickListener {
            AnalyticsTracker.trackClick("login", "login_button")
            viewModel.login(
                binding.editEmail.text?.toString()?.trim().orEmpty(),
                binding.editPassword.text?.toString().orEmpty()
            )
        }

        viewModel.loginState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ResultState.Loading -> {
                    binding.progressLogin.visibility = View.VISIBLE
                    binding.buttonLogin.isEnabled = false
                    binding.textError.visibility = View.GONE
                }

                is ResultState.Success -> {
                    binding.progressLogin.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                    sessionManager.saveToken(result.data)
                    if (binding.switchRememberMe.isChecked) {
                        sessionManager.saveRememberedCredentials(
                            binding.editEmail.text?.toString()?.trim().orEmpty(),
                            binding.editPassword.text?.toString().orEmpty()
                        )
                    } else {
                        sessionManager.clearRememberedCredentials()
                    }
                    sendLoginEventToLauncher(binding.editEmail.text?.toString()?.trim().orEmpty())
                    PushTokenRegistrar.register()
                    findNavController().navigate(R.id.action_login_to_orders)
                }

                is ResultState.Error -> {
                    binding.progressLogin.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                    binding.textError.visibility = View.VISIBLE
                    binding.textError.text =
                        result.message ?: getString(R.string.login_error_generic)
                }
            }
        }
    }

    private fun updateFormValid() {
        val email = binding.editEmail.text?.toString().orEmpty()
        val password = binding.editPassword.text?.toString().orEmpty()
        binding.buttonLogin.isEnabled = email.isEmailFormatValid() && password.isNotEmpty()
    }

    /** Best-effort - the launcher uses this only to know who's currently using the device, so a
     * missed broadcast (launcher process not up yet) isn't worth retrying or acking. */
    private fun sendLoginEventToLauncher(email: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(java.util.Date())
        val intent = Intent(KioskIpc.ACTION_LOGIN_EVENT)
            .setClassName(KioskIpc.LAUNCHER_PACKAGE, "karika.distribucija.ba.launcher.KioskEventReceiver")
            .putExtra(KioskIpc.EXTRA_TOKEN, BuildConfig.KIOSK_IPC_TOKEN)
            .putExtra(KioskIpc.EXTRA_USER_EMAIL, email)
            .putExtra(KioskIpc.EXTRA_LOGIN_TIMESTAMP, timestamp)
        runCatching { requireContext().sendBroadcast(intent) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
