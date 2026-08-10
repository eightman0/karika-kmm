package karika.distribucija.ba.salesrep.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentCustomerMessagesBinding
import karika.distribucija.ba.salesrep.model.Conversation

/** Mirrors composeApp's SalesCustomerMessagesView.kt. */
open class CustomerMessagesFragment : Fragment() {

    private var _binding: FragmentCustomerMessagesBinding? = null
    private val binding get() = _binding!!
    protected open val viewModel: CustomerMessagesViewModel by viewModels()
    private lateinit var adapter: ConversationCardAdapter

    protected open val screenTitleRes: Int = R.string.customer_messages_title
    protected open val newMessageActionId: Int = R.id.action_customer_messages_to_new_message
    protected open val conversationActionId: Int = R.id.action_customer_messages_to_conversation
    protected open val emptyIconRes: Int = R.drawable.ic_messages
    protected open val emptySubtitleRes: Int = R.string.customer_messages_empty_subtitle
    protected open fun displayName(conversation: Conversation): String = conversation.customerName()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(screenTitleRes)
        binding.iconEmpty.setImageResource(emptyIconRes)
        binding.textEmptySubtitle.text = getString(emptySubtitleRes)

        adapter = ConversationCardAdapter(
            displayName = { conversation -> displayName(conversation) },
            onClick = { conversation -> openConversation(conversation) }
        )
        binding.recyclerConversations.adapter = adapter
        binding.recyclerConversations.layoutManager = LinearLayoutManager(requireContext())

        binding.pillFilterAll.setOnClickListener { selectFilter(CustomerMessagesViewModel.Filter.ALL) }
        binding.pillFilterSent.setOnClickListener { selectFilter(CustomerMessagesViewModel.Filter.SENT) }
        binding.pillFilterReceived.setOnClickListener { selectFilter(CustomerMessagesViewModel.Filter.RECEIVED) }

        binding.buttonNewMessage.setOnClickListener {
            findNavController().navigate(newMessageActionId)
        }

        viewModel.filteredConversations.observe(viewLifecycleOwner) { conversations ->
            adapter.submitList(conversations)
            renderEmptyState()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectFilter(filter: CustomerMessagesViewModel.Filter) {
        viewModel.setFilter(filter)
        stylePill(binding.pillFilterAll, filter == CustomerMessagesViewModel.Filter.ALL)
        stylePill(binding.pillFilterSent, filter == CustomerMessagesViewModel.Filter.SENT)
        stylePill(binding.pillFilterReceived, filter == CustomerMessagesViewModel.Filter.RECEIVED)
        renderEmptyState()
    }

    private fun stylePill(pill: TextView, selected: Boolean) {
        pill.setBackgroundResource(
            if (selected) R.drawable.bg_message_filter_selected else R.drawable.bg_message_filter_unselected
        )
        pill.setTextColor(requireContext().getColor(if (selected) R.color.karika_white else R.color.karika_gray2))
    }

    private fun renderEmptyState() {
        val conversations = viewModel.filteredConversations.value.orEmpty()
        binding.layoutEmpty.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE
        binding.textEmptyTitle.text = getString(
            when (viewModel.filter.value) {
                CustomerMessagesViewModel.Filter.SENT -> R.string.customer_messages_empty_sent
                CustomerMessagesViewModel.Filter.RECEIVED -> R.string.customer_messages_empty_received
                else -> R.string.customer_messages_empty_all
            }
        )
    }

    private fun openConversation(conversation: Conversation) {
        findNavController().navigate(
            conversationActionId,
            bundleOf(
                "threadId" to conversation.id.orEmpty(),
                "customerName" to displayName(conversation),
                "subject" to conversation.subject,
                "receiverId" to (conversation.receiverIdInt() ?: -1)
            )
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
