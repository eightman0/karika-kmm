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
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerNewMessageBinding
import karika.distribucija.ba.salesrep.model.OperationalCustomer
import karika.distribucija.ba.salesrep.util.applyImeBottomPadding

/** Mirrors composeApp's SalesCustomerNewMessageView.kt. */
class CustomerNewMessageFragment : Fragment() {

    private var _binding: FragmentCustomerNewMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CustomerNewMessageViewModel by viewModels()
    private lateinit var messageAdapter: CustomerMessageAdapter
    private lateinit var customerAdapter: CustomerRowAdapter
    private var customerFieldFocused = false
    private var customerSearchWatcher: android.text.TextWatcher? = null

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
            counterpartName = { viewModel.selectedCustomer.value?.let { it.company ?: it.fullName }.orEmpty() },
            formatTimestamp = { formatTime(it) }
        )
        binding.recyclerMessages.adapter = messageAdapter
        binding.recyclerMessages.layoutManager = LinearLayoutManager(requireContext())

        customerAdapter = CustomerRowAdapter { customer -> selectCustomer(customer) }
        binding.recyclerCustomers.adapter = customerAdapter
        binding.recyclerCustomers.layoutManager = LinearLayoutManager(requireContext())

        binding.editSubject.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            viewModel.setSubject(text?.toString().orEmpty())
        })

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

        binding.buttonSend.setOnClickListener {
            val text = binding.editMessage.text?.toString().orEmpty()
            viewModel.send(text)
            binding.editMessage.setText("")
        }

        viewModel.customers.observe(viewLifecycleOwner) { renderCustomerField() }
        viewModel.selectedCustomer.observe(viewLifecycleOwner) { renderCustomerField() }
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

        renderCustomerField()
        renderSendButton()
    }

    /** Sets the selection in the ViewModel, then mirrors the name into the search field without
     * letting that setText() re-fire the search TextWatcher - [viewModel]'s setCustomerSearch()
     * clears the selection on every text change (that's how live-filtering-while-typing detects
     * "the user is typing something new"), so a watcher left attached during this programmatic
     * setText() would immediately undo the selection we just made - and worse, send() would
     * then submit with no receiverId at all. */
    private fun selectCustomer(customer: OperationalCustomer) {
        viewModel.selectCustomer(customer)
        customerSearchWatcher?.let { binding.editCustomerSearch.removeTextChangedListener(it) }
        binding.editCustomerSearch.setText(customer.company ?: customer.fullName)
        customerSearchWatcher?.let { binding.editCustomerSearch.addTextChangedListener(it) }
        customerFieldFocused = false
        binding.editCustomerSearch.clearFocus()
    }

    /** Mirrors the recipient field's three visual states: default, focused, and focused-with-
     * dropdown-open (top-rounded-only), matching the internal-new-message screen's pattern. */
    private fun renderCustomerField() {
        val selected = viewModel.selectedCustomer.value
        val results = viewModel.customers.value.orEmpty().take(6)
        val showDropdown = customerFieldFocused && selected == null && results.isNotEmpty()

        binding.editCustomerSearch.isFocusable = selected == null
        binding.editCustomerSearch.isFocusableInTouchMode = selected == null
        binding.editCustomerSearch.setTextColor(
            requireContext().getColor(if (selected != null) R.color.karika_blue else R.color.karika_gray2)
        )

        val query = binding.editCustomerSearch.text?.toString().orEmpty()
        binding.buttonClearCustomer.visibility = if (selected != null || query.isNotEmpty()) View.VISIBLE else View.GONE

        binding.recyclerCustomers.visibility = if (showDropdown) View.VISIBLE else View.GONE
        if (showDropdown) customerAdapter.submitList(results)

        binding.recipientFieldContainer.setBackgroundResource(
            when {
                showDropdown -> R.drawable.bg_message_field_open
                customerFieldFocused -> R.drawable.bg_message_field_focused
                else -> R.drawable.bg_message_field_default
            }
        )
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
