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
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerConversationBinding
import karika.distribucija.ba.salesrep.model.ChatMessage
import karika.distribucija.ba.salesrep.util.AttachmentPicker
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.inSarajevo
import karika.distribucija.ba.salesrep.util.openPdfExternally

/** Mirrors composeApp's SalesCustomerConversationView.kt. */
open class CustomerConversationFragment : Fragment() {

    private var _binding: FragmentCustomerConversationBinding? = null
    private val binding get() = _binding!!
    protected open val viewModel: CustomerConversationViewModel by viewModels()
    private lateinit var adapter: CustomerMessageAdapter

    /** The action-bar title: the customer's name ("-" without one); Admin falls back to "Poruka". */
    protected open val screenTitle: String get() = viewModel.counterpartName ?: "-"

    /** The bubble's "other party" label - the message's sender_display_name for customers, the
     * literal "Administrator" on Admin's screen. */
    protected open val senderName: (ChatMessage) -> String? = { it.senderDisplayName }

    private val attachmentPicker = AttachmentPicker(this) { name, bytes -> viewModel.setAttachment(name, bytes) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = screenTitle
        binding.root.applyImeBottomPadding()

        adapter = CustomerMessageAdapter(
            lifecycleOwner = viewLifecycleOwner,
            senderName = { senderName(it) },
            formatTimestamp = { it?.inSarajevo().orEmpty() },
            onImageClick = { url -> ImagePreviewFragment.newInstance(url).show(childFragmentManager, "image_preview") },
            onPdfClick = { url -> requireContext().openPdfExternally(url) }
        )
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        binding.editMessage.addTextChangedListener(onTextChanged = { _, _, _, _ -> updateSendButtonState() })

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
            viewModel.sendMessage(text)
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
    }

    /** Mirrors SalesChatInput's pending attachment (thumbnail or chip, see renderPendingAttachment);
     * the attach button's icon tints blue while a pick is pending. */
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
        updateSendButtonState()
    }

    private fun updateSendButtonState() {
        val canSend = !binding.editMessage.text.isNullOrBlank() || viewModel.attachment.value != null
        binding.buttonSend.isEnabled = canSend
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
