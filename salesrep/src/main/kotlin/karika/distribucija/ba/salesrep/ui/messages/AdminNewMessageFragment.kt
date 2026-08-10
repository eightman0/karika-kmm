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
import karika.distribucija.ba.salesrep.databinding.FragmentAdminNewMessageBinding
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding

/** Mirrors composeApp's SalesAdminNewMessageView.kt - the subject-header + bubble-list + input
 * bar, with no recipient picker (unlike CustomerNewMessageFragment, which has one). */
class AdminNewMessageFragment : Fragment() {

    private var _binding: FragmentAdminNewMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminNewMessageViewModel by viewModels()
    private lateinit var adapter: CustomerMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminNewMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.admin_new_message_default_title)
        binding.root.applyImeBottomPadding()

        adapter = CustomerMessageAdapter(
            counterpartName = { getString(R.string.admin_conversation_counterpart_label) },
            formatTimestamp = { formatTime(it) },
            isMine = { it.isVendorMessage() }
        )
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        binding.editSubject.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setSubject(text?.toString().orEmpty())
        })

        binding.editMessage.addTextChangedListener(onTextChanged = { _, _, _, _ -> renderSendButton() })

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString().orEmpty()
            viewModel.send(text)
            binding.editMessage.setText("")
        }

        viewModel.threadId.observe(viewLifecycleOwner) { threadId ->
            binding.layoutComposeHeader.visibility = if (threadId == null) View.VISIBLE else View.GONE
            val subject = viewModel.subject.value
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                if (threadId != null && !subject.isNullOrBlank()) subject
                else getString(R.string.admin_new_message_default_title)
        }
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.recyclerMessages.scrollToPosition(messages.size - 1)
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        renderSendButton()
    }

    private fun renderSendButton() {
        val canSend = !binding.editMessage.text.isNullOrBlank()
        binding.buttonSend.setBackgroundResource(
            if (canSend) R.drawable.bg_message_send_active else R.drawable.bg_message_send_inactive
        )
        binding.buttonSend.isEnabled = canSend
    }

    private fun formatTime(raw: String?): String {
        if (raw == null) return ""
        val timePart = raw.split(" ").getOrNull(1) ?: return ""
        val parts = timePart.split(":")
        return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
