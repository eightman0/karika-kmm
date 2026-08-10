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
import karika.distribucija.ba.salesrep.databinding.FragmentInternalNewMessageBinding
import karika.distribucija.ba.salesrep.model.StaffRecipient
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding

/** Mirrors composeApp's SalesInternalNewMessageView.kt. */
class InternalNewMessageFragment : Fragment() {

    private var _binding: FragmentInternalNewMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InternalNewMessageViewModel by viewModels()
    private lateinit var messageAdapter: InternalMessageAdapter
    private lateinit var recipientAdapter: RecipientRowAdapter
    private var recipientFieldFocused = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInternalNewMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.internal_new_message_title)
        binding.root.applyImeBottomPadding()

        messageAdapter = InternalMessageAdapter { viewModel.selectedRecipient.value?.name.orEmpty() }
        binding.recyclerMessages.adapter = messageAdapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        recipientAdapter = RecipientRowAdapter { recipient -> selectRecipient(recipient) }
        binding.recyclerRecipients.adapter = recipientAdapter
        binding.recyclerRecipients.layoutManager = LinearLayoutManager(requireContext())

        binding.editSubject.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setSubject(text?.toString().orEmpty())
        })

        binding.editRecipientSearch.setOnFocusChangeListener { _, hasFocus ->
            recipientFieldFocused = hasFocus
            renderRecipientField()
        }
        binding.editRecipientSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setRecipientSearch(text?.toString().orEmpty())
        })
        binding.buttonClearRecipient.setOnClickListener {
            viewModel.clearRecipient()
            binding.editRecipientSearch.setText("")
            binding.editRecipientSearch.requestFocus()
        }

        binding.editMessage.addTextChangedListener(onTextChanged = { _, _, _, _ -> renderSendButton() })

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString().orEmpty()
            viewModel.send(text)
            binding.editMessage.setText("")
        }

        viewModel.filteredRecipients.observe(viewLifecycleOwner) { renderRecipientField() }
        viewModel.selectedRecipient.observe(viewLifecycleOwner) {
            renderRecipientField()
            renderSendButton()
        }
        viewModel.threadId.observe(viewLifecycleOwner) { threadId ->
            binding.layoutComposeHeader.visibility = if (threadId == null) View.VISIBLE else View.GONE
        }
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.recyclerMessages.scrollToPosition(messages.size - 1)
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        renderRecipientField()
        renderSendButton()
    }

    private fun selectRecipient(recipient: StaffRecipient) {
        viewModel.selectRecipient(recipient)
        binding.editRecipientSearch.setText(recipient.name)
        recipientFieldFocused = false
        binding.editRecipientSearch.clearFocus()
    }

    /** Mirrors the recipient field's three visual states: default, focused, and focused-with-
     * dropdown-open (top-rounded-only, matching the discount form's search field pattern). */
    private fun renderRecipientField() {
        val selected = viewModel.selectedRecipient.value
        val results = viewModel.filteredRecipients.value.orEmpty().take(6)
        val showDropdown = recipientFieldFocused && selected == null && results.isNotEmpty()

        binding.editRecipientSearch.isFocusable = selected == null
        binding.editRecipientSearch.isFocusableInTouchMode = selected == null
        binding.editRecipientSearch.setTextColor(
            requireContext().getColor(if (selected != null) R.color.karika_blue else R.color.karika_gray2)
        )

        val query = binding.editRecipientSearch.text?.toString().orEmpty()
        binding.buttonClearRecipient.visibility = if (selected != null || query.isNotEmpty()) View.VISIBLE else View.GONE

        binding.recyclerRecipients.visibility = if (showDropdown) View.VISIBLE else View.GONE
        if (showDropdown) recipientAdapter.submitList(results)

        binding.recipientFieldContainer.setBackgroundResource(
            when {
                showDropdown -> R.drawable.bg_message_field_open
                recipientFieldFocused -> R.drawable.bg_message_field_focused
                else -> R.drawable.bg_message_field_default
            }
        )
    }

    private fun renderSendButton() {
        val canSend = !binding.editMessage.text.isNullOrBlank() && viewModel.selectedRecipient.value != null
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
