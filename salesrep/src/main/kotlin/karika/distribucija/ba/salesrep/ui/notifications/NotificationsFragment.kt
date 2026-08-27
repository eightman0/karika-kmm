package karika.distribucija.ba.salesrep.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentNotificationsBinding
import karika.distribucija.ba.salesrep.notifications.NotificationDestination

/** Mirrors composeApp's SalesNotificationsView.kt. */
class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NotificationAdapter(onClick = { viewModel.markAsRead(it) })
        binding.recyclerNotifications.adapter = adapter
        binding.recyclerNotifications.layoutManager = LinearLayoutManager(requireContext())

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            adapter.submitList(notifications)
            binding.textNotificationsEmpty.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) { destination ->
            if (destination != null) {
                navigateTo(destination)
                viewModel.clearNavigation()
            }
        }
    }

    private fun navigateTo(destination: NotificationDestination) {
        when (destination) {
            is NotificationDestination.OrderDetail -> findNavController().navigate(
                R.id.action_notifications_to_order_detail,
                bundleOf(
                    "orderId" to 0L,
                    "incrementId" to destination.orderId,
                    "customerId" to 0L,
                    "customerName" to null,
                    "grandTotal" to 0f,
                    "status" to "",
                    "createdAt" to null
                )
            )

            is NotificationDestination.Conversation -> findNavController().navigate(
                if (destination.admin) R.id.action_notifications_to_admin_conversation
                else R.id.action_notifications_to_customer_conversation,
                bundleOf(
                    "threadId" to destination.threadId,
                    "customerName" to destination.customerName,
                    "subject" to destination.subject,
                    "receiverId" to destination.receiverId
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
