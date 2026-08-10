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
import karika.distribucija.ba.salesrep.util.AttachmentPicker
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.isImageAttachmentFile

/** Mirrors composeApp's SalesAdminNewMessageView.kt - the subject-header + bubble-list + input
 * bar, with no recipient picker (unlike CustomerNewMessageFragment, which has one). */
class AdminNewMessageFragment : Fragment() {

    private var _binding: FragmentAdminNewMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminNewMessageViewModel by viewModels()
    private lateinit var adapter: CustomerMessageAdapter
    private val attachmentPicker = AttachmentPicker(this) { name, bytes -> viewModel.setAttachment(name, bytes) }

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
            lifecycleOwner = viewLifecycleOwner,
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

        binding.buttonAttach.setOnClickListener {
            AttachSheet(
                onPickFile = { attachmentPicker.pickFile() },
                onPickPhoto = { attachmentPicker.pickPhoto() }
            ).show(childFragmentManager, "attach_sheet")
        }

        binding.buttonRemoveAttachment.setOnClickListener { viewModel.clearAttachment() }

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
        viewModel.attachment.observe(viewLifecycleOwner) { renderAttachment(it) }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        renderSendButton()
    }

    /** Mirrors the pending-attachment chip in SalesAdminNewMessageView.kt (see
     * CustomerConversationFragment's identical renderAttachment for the exact Compose match). */
    private fun renderAttachment(attachment: Pair<String, ByteArray>?) {
        binding.layoutPendingAttachment.visibility = if (attachment != null) View.VISIBLE else View.GONE
        if (attachment != null) {
            binding.iconAttachmentType.setImageResource(
                if (isImageAttachmentFile(attachment.first)) R.drawable.ic_photo else R.drawable.ic_attachment
            )
            binding.textAttachmentName.text = attachment.first.take(32)
        }
        binding.iconAttach.setColorFilter(
            requireContext().getColor(if (attachment != null) R.color.karika_blue else R.color.karika_gray6)
        )
        renderSendButton()
    }

    private fun renderSendButton() {
        val canSend = !binding.editMessage.text.isNullOrBlank() || viewModel.attachment.value != null
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
