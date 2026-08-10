package karika.distribucija.ba.salesrep.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentInternalConversationBinding
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding

/** Mirrors composeApp's SalesInternalConversationView.kt. */
class InternalConversationFragment : Fragment() {

    private var _binding: FragmentInternalConversationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InternalConversationViewModel by viewModels()
    private lateinit var adapter: InternalMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInternalConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = viewModel.counterpartName
        binding.root.applyImeBottomPadding()

        adapter = InternalMessageAdapter { viewModel.counterpartName }
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        binding.editMessage.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val canSend = !text.isNullOrBlank()
            binding.buttonSend.setBackgroundResource(
                if (canSend) R.drawable.bg_message_send_active else R.drawable.bg_message_send_inactive
            )
            binding.buttonSend.isEnabled = canSend
        })

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString().orEmpty()
            if (text.isBlank()) return@setOnClickListener
            viewModel.sendMessage(text)
            binding.editMessage.setText("")
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.recyclerMessages.scrollToPosition(messages.size - 1)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
