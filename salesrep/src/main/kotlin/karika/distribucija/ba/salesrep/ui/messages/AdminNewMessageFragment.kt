package karika.distribucija.ba.salesrep.ui.messages

import karika.distribucija.ba.salesrep.util.showSnack
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentAdminNewMessageBinding
import karika.distribucija.ba.salesrep.util.AttachmentPicker
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.inSarajevo
import karika.distribucija.ba.salesrep.util.openPdfExternally

/** Mirrors composeApp's SalesAdminNewMessageView.kt - the bubble list + input bar, with no
 * recipient picker (unlike CustomerNewMessageFragment, which has one). */
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
            senderName = { getString(R.string.admin_conversation_counterpart_label) },
            formatTimestamp = { formatTime(it) },
            onImageClick = { url -> ImagePreviewFragment.newInstance(url).show(childFragmentManager, "image_preview") },
            onPdfClick = { url -> requireContext().openPdfExternally(url) }
        )
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        binding.editMessage.addTextChangedListener(onTextChanged = { _, _, _, _ -> renderSendButton() })

        binding.buttonAttach.setOnClickListener {
            AttachSheet(
                onTakePhoto = { attachmentPicker.takePhoto() },
                onPickPhoto = { attachmentPicker.pickPhoto() },
                onPickFile = { attachmentPicker.pickFile() }
            ).show(childFragmentManager, "attach_sheet")
        }

        binding.buttonRemoveAttachment.setOnClickListener { viewModel.clearAttachment() }

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString().orEmpty()
            viewModel.send(text)
            binding.editMessage.setText("")
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.recyclerMessages.scrollToPosition(messages.size - 1)
            }
        }
        viewModel.attachment.observe(viewLifecycleOwner) { renderAttachment(it) }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) showSnack(message)
        }

        renderSendButton()
    }

    /** Mirrors SalesChatInput's pending attachment in SalesAdminNewMessageView.kt (see renderPendingAttachment). */
    private fun renderAttachment(attachment: Pair<String, ByteArray>?) {
        renderPendingAttachment(
            container = binding.layoutPendingAttachment,
            preview = binding.imageAttachmentPreview,
            chip = binding.chipAttachment,
            icon = binding.iconAttachmentType,
            name = binding.textAttachmentName,
            attachment = attachment
        )
        binding.iconAttach.setColorFilter(
            requireContext().getColor(if (attachment != null) R.color.k_accent else R.color.k_ink)
        )
        renderSendButton()
    }

    private fun renderSendButton() {
        val canSend = !binding.editMessage.text.isNullOrBlank() || viewModel.attachment.value != null
        binding.buttonSend.isEnabled = canSend
    }

    private fun formatTime(raw: String?): String {
        if (raw == null) return ""
        val timePart = raw.inSarajevo().split(" ").getOrNull(1) ?: return ""
        val parts = timePart.split(":")
        return if (parts.size >= 2) "${parts[0]}:${parts[1]}" else timePart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
