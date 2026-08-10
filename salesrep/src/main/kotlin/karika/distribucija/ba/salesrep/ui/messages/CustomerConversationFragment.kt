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
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerConversationBinding
import karika.distribucija.ba.salesrep.model.Message
import karika.distribucija.ba.salesrep.util.AttachmentPicker
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.isImageAttachmentFile
import karika.distribucija.ba.salesrep.util.openPdfExternally

/** Mirrors composeApp's SalesCustomerConversationView.kt. */
open class CustomerConversationFragment : Fragment() {

    private var _binding: FragmentCustomerConversationBinding? = null
    private val binding get() = _binding!!
    protected open val viewModel: CustomerConversationViewModel by viewModels()
    private lateinit var adapter: CustomerMessageAdapter

    /** Admin's action-bar title is `conversation.subject ?: "Poruka"`, not the counterpart's
     * name (unlike Customer, whose title IS the customer's name) - see SalesDashboardView.kt's
     * AdminConversation vs CustomerConversation SalesDetailTopBar wiring. */
    protected open val screenTitle: String get() = viewModel.customerName

    /** The bubble's "other party" label - Customer shows the customer's name, Admin hardcodes
     * the literal "Administrator" (never derived from the conversation). */
    protected open val counterpartDisplayName: String get() = viewModel.customerName

    /** "Is this my message" - Customer's Compose view checks `sender == "vendor"`, Admin's
     * checks `receiverId == "0" || sender == "customer"` (`Message.isVendorMessage()`) - a
     * genuine difference between the two Compose source files, not a copy-paste slip. */
    protected open val isMine: (Message) -> Boolean = { it.isVendor() }

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
            counterpartName = { counterpartDisplayName },
            formatTimestamp = { it.orEmpty() },
            isMine = isMine,
            onImageClick = { url -> ImagePreviewFragment.newInstance(url).show(childFragmentManager, "image_preview") },
            onPdfClick = { url -> requireContext().openPdfExternally(url) }
        )
        binding.recyclerMessages.adapter = adapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        binding.editMessage.addTextChangedListener(onTextChanged = { _, _, _, _ -> updateSendButtonState() })

        binding.buttonAttach.setOnClickListener {
            AttachSheet(
                onPickFile = { attachmentPicker.pickFile() },
                onPickPhoto = { attachmentPicker.pickPhoto() }
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
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    /** Mirrors the pending-attachment chip in SalesCustomerConversationView.kt: icon depends on
     * file type, name is truncated to 32 chars, and the attach button's icon tints blue while a
     * pick is pending - matching `attachment!!.first.take(32)` / `if (attachment != null) Blue
     * else Gray6` exactly. */
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
        updateSendButtonState()
    }

    private fun updateSendButtonState() {
        val canSend = !binding.editMessage.text.isNullOrBlank() || viewModel.attachment.value != null
        binding.buttonSend.setBackgroundResource(
            if (canSend) R.drawable.bg_message_send_active else R.drawable.bg_message_send_inactive
        )
        binding.buttonSend.isEnabled = canSend
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
