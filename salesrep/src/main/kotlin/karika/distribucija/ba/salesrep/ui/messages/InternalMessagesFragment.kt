package karika.distribucija.ba.salesrep.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentInternalMessagesBinding
import karika.distribucija.ba.salesrep.model.StaffThread

/** Mirrors composeApp's SalesInternalMessagesView.kt. */
class InternalMessagesFragment : Fragment() {

    private var _binding: FragmentInternalMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InternalMessagesViewModel by viewModels()
    private lateinit var adapter: InternalThreadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInternalMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.internal_messages_title)

        adapter = InternalThreadAdapter { thread -> openConversation(thread) }
        binding.recyclerThreads.adapter = adapter
        binding.recyclerThreads.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonNewMessage.setOnClickListener {
            findNavController().navigate(R.id.action_internal_messages_to_new_message)
        }

        viewModel.threads.observe(viewLifecycleOwner) { threads ->
            adapter.submitList(threads)
            binding.layoutEmpty.visibility = if (threads.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openConversation(thread: StaffThread) {
        findNavController().navigate(
            R.id.action_internal_messages_to_conversation,
            bundleOf(
                "threadId" to thread.threadId,
                "counterpartName" to thread.counterpartName
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
