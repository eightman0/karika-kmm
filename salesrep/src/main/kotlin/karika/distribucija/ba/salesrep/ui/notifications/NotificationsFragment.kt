package karika.distribucija.ba.salesrep.ui.notifications

import karika.distribucija.ba.salesrep.util.showSnack
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.databinding.FragmentNotificationsBinding
import karika.distribucija.ba.salesrep.notifications.NotificationDestination
import karika.distribucija.ba.salesrep.notifications.openChatConversation

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

        requireActivity().addMenuProvider(readFilterMenu, viewLifecycleOwner, Lifecycle.State.RESUMED)
        viewModel.readFilter.observe(viewLifecycleOwner) { requireActivity().invalidateOptionsMenu() }

        adapter = NotificationAdapter(onClick = { viewModel.markAsRead(it) })
        val markAll = MarkAllReadAdapter(onClick = { viewModel.markAllAsRead() })
        binding.recyclerNotifications.adapter = ConcatAdapter(markAll, adapter)
        binding.recyclerNotifications.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.load() }

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            adapter.submitList(notifications)
            markAll.visible = notifications.any { !it.isRead }
            binding.layoutEmpty.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) showSnack(message)
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) { destination ->
            if (destination != null) {
                navigateTo(destination)
                viewModel.clearNavigation()
            }
        }
    }

    /** ReadFilterDropdown in the top bar, as composeApp's SalesDashboardView shows for Obavijesti:
     * bordered label + chevron, opening Sve / Nepročitano / Pročitano. */
    private val readFilterMenu = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.notifications_menu, menu)
            val chooser = menu.findItem(R.id.action_read_filter).actionView ?: return
            val selected = viewModel.readFilter.value ?: NotificationsViewModel.ReadFilter.ALL
            chooser.findViewById<TextView>(R.id.text_read_filter).text = getString(selected.labelRes())
            chooser.setOnClickListener { showReadFilterMenu(it) }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
    }

    private fun showReadFilterMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        val selected = viewModel.readFilter.value
        NotificationsViewModel.ReadFilter.entries.forEachIndexed { index, filter ->
            // Selected option in blue bold, the rest plain - like the Compose DropdownMenu.
            val label = android.text.SpannableString(getString(filter.labelRes()))
            if (filter == selected) {
                label.setSpan(android.text.style.ForegroundColorSpan(requireContext().getColor(R.color.k_accent)), 0, label.length, 0)
                label.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, label.length, 0)
            }
            popup.menu.add(0, index, index, label)
        }
        popup.setOnMenuItemClickListener { item ->
            viewModel.setReadFilter(NotificationsViewModel.ReadFilter.entries[item.itemId])
            true
        }
        popup.show()
    }

    private fun NotificationsViewModel.ReadFilter.labelRes(): Int = when (this) {
        NotificationsViewModel.ReadFilter.ALL -> R.string.notifications_filter_all
        NotificationsViewModel.ReadFilter.UNREAD -> R.string.notifications_filter_unread
        NotificationsViewModel.ReadFilter.READ -> R.string.notifications_filter_read
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
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

            is NotificationDestination.Conversation ->
                viewLifecycleOwner.openChatConversation(findNavController(), destination.conversationId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
