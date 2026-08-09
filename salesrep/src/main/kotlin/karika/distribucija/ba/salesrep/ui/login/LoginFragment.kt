package karika.distribucija.ba.salesrep.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.SalesRepApp
import karika.distribucija.ba.salesrep.databinding.FragmentLoginBinding
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.util.isEmailFormatValid

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

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

        updateFormValid()

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
            Toast.makeText(requireContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogin.setOnClickListener {
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
                    (requireActivity().application as SalesRepApp).sessionManager.saveToken(result.data)
                    findNavController().navigate(R.id.action_login_to_orders)
                }

                is ResultState.Error -> {
                    binding.progressLogin.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                    binding.textError.visibility = View.VISIBLE
                    binding.textError.text = result.message ?: getString(R.string.login_error_generic)
                }
            }
        }
    }

    private fun updateFormValid() {
        val email = binding.editEmail.text?.toString().orEmpty()
        val password = binding.editPassword.text?.toString().orEmpty()
        binding.buttonLogin.isEnabled = email.isEmailFormatValid() && password.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
