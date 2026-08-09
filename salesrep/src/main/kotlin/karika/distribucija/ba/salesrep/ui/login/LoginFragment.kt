package karika.distribucija.ba.salesrep.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.SalesRepApp
import karika.distribucija.ba.salesrep.databinding.FragmentLoginBinding
import karika.distribucija.ba.salesrep.model.ResultState

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

        binding.buttonLogin.setOnClickListener {
            viewModel.login(
                binding.editUsername.text?.toString()?.trim().orEmpty(),
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
