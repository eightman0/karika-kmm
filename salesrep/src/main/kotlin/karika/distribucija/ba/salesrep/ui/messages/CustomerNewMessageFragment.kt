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
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerNewMessageBinding
import karika.distribucija.ba.salesrep.model.ChatRecipient
import karika.distribucija.ba.salesrep.util.AttachmentPicker
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding
import karika.distribucija.ba.salesrep.util.inSarajevo
import karika.distribucija.ba.salesrep.util.openPdfExternally

/** Mirrors composeApp's SalesCustomerNewMessageView.kt. */
class CustomerNewMessageFragment : Fragment() {

    private var _binding: FragmentCustomerNewMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerNewMessageViewModel by viewModels()
    private lateinit var messageAdapter: CustomerMessageAdapter
    private lateinit var customerAdapter: CustomerRowAdapter
    private var customerFieldFocused = false
    private var customerSearchWatcher: android.text.TextWatcher? = null
    private val attachmentPicker = AttachmentPicker(this) { name, bytes -> viewModel.setAttachment(name, bytes) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerNewMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.customer_new_message_title)
        binding.root.applyImeBottomPadding()

        messageAdapter = CustomerMessageAdapter(
            lifecycleOwner = viewLifecycleOwner,
            senderName = { it.senderDisplayName },
            formatTimestamp = { formatTime(it) },
            onImageClick = { url -> ImagePreviewFragment.newInstance(url).show(childFragmentManager, "image_preview") },
            onPdfClick = { url -> requireContext().openPdfExternally(url) }
        )
        binding.recyclerMessages.adapter = messageAdapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        customerAdapter = CustomerRowAdapter { customer -> selectCustomer(customer) }
        binding.recyclerCustomers.adapter = customerAdapter
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(requireContext())

        binding.editCustomerSearch.setOnFocusChangeListener { _, hasFocus ->
            customerFieldFocused = hasFocus
            renderCustomerField()
        }
        customerSearchWatcher = binding.editCustomerSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setCustomerSearch(text?.toString().orEmpty())
        })
        binding.buttonClearCustomer.setOnClickListener {
            viewModel.clearCustomer()
            binding.editCustomerSearch.setText("")
            binding.editCustomerSearch.requestFocus()
        }

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

        viewModel.customers.observe(viewLifecycleOwner) { renderCustomerField() }
        viewModel.selectedCustomer.observe(viewLifecycleOwner) { selected ->
            // The title is the picked customer's name, "Nova poruka" until one is picked.
            (activity as? AppCompatActivity)?.supportActionBar?.title =
                selected?.name ?: getString(R.string.customer_new_message_title)
            renderCustomerField()
            renderSendButton()
        }
        viewModel.conversationId.observe(viewLifecycleOwner) { conversationId ->
            binding.layoutComposeHeader.visibility = if (conversationId == null) View.VISIBLE else View.GONE
            renderSendButton()
        }
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.recyclerMessages.scrollToPosition(messages.size - 1)
            }
        }
        viewModel.attachment.observe(viewLifecycleOwner) { renderAttachment(it) }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) showSnack(message)
        }

        viewModel.selectedCustomer.value?.let { selected ->
            setCustomerFieldText(selected.name.orEmpty())
        }
        renderCustomerField()
        renderSendButton()
    }

    /** Mirrors SalesChatInput's pending attachment in SalesCustomerNewMessageView.kt (see renderPendingAttachment). */
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

    /** Sets the selection in the ViewModel, then mirrors the name into the search field without
     * letting that setText() re-fire the search TextWatcher - [viewModel]'s setCustomerSearch()
     * clears the selection on every text change (that's how live-filtering-while-typing detects
     * "the user is typing something new"), so a watcher left attached during this programmatic
     * setText() would immediately undo the selection we just made. */
    private fun selectCustomer(customer: ChatRecipient) {
        viewModel.selectCustomer(customer)
        setCustomerFieldText(customer.name.orEmpty())
        customerFieldFocused = false
        binding.editCustomerSearch.clearFocus()
    }

    private fun setCustomerFieldText(text: String) {
        customerSearchWatcher?.let { binding.editCustomerSearch.removeTextChangedListener(it) }
        binding.editCustomerSearch.setText(text)
        customerSearchWatcher?.let { binding.editCustomerSearch.addTextChangedListener(it) }
    }

    /** Mirrors the recipient field's three visual states: default, focused, and focused-with-
     * dropdown-open (top-rounded-only), matching the internal-new-message screen's pattern. */
    private fun renderCustomerField() {
        val selected = viewModel.selectedCustomer.value
        val results = viewModel.customers.value.orEmpty().take(6)
        val showDropdown = customerFieldFocused && selected == null && results.isNotEmpty()

        // A customer given by the customer list is fixed (SalesRecipientPicker's locked mode).
        binding.editCustomerSearch.isFocusable = selected == null
        binding.editCustomerSearch.isFocusableInTouchMode = selected == null
        binding.editCustomerSearch.setTextColor(
            requireContext().getColor(if (selected != null) R.color.k_accent else R.color.k_ink)
        )

        val query = binding.editCustomerSearch.text?.toString().orEmpty()
        binding.buttonClearCustomer.visibility =
            if (!viewModel.customerLocked && (selected != null || query.isNotEmpty())) View.VISIBLE else View.GONE

        binding.recyclerCustomers.visibility = if (showDropdown) View.VISIBLE else View.GONE
        if (showDropdown) customerAdapter.submitList(results)

        binding.recipientFieldContainer.setBackgroundResource(
            when {
                showDropdown -> R.drawable.bg_k_msg_field_open
                customerFieldFocused -> R.drawable.bg_k_field_outline_focused
                else -> R.drawable.bg_k_field_outline
            }
        )
    }

    /** Mirrors SalesCustomerNewMessageView's canSend: text or an attachment, and a customer
     * (or the already started conversation). */
    private fun renderSendButton() {
        val hasContent = !binding.editMessage.text.isNullOrBlank() || viewModel.attachment.value != null
        val hasTarget = viewModel.conversationId.value != null || viewModel.selectedCustomer.value != null
        binding.buttonSend.isEnabled = hasContent && hasTarget
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
