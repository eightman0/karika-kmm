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

        adapter = CustomerMessageAdapter(
            counterpartName = { counterpartDisplayName },
            formatTimestamp = { it.orEmpty() }
        )
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
