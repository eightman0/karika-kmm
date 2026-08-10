package karika.distribucija.ba.salesrep.ui.messages

import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.model.Conversation

/** Mirrors composeApp's SalesAdminMessagesView.kt - reuses CustomerMessagesFragment's list/filter
 * UI wholesale; only the title, empty-state icon/subtitle, action ids, and the displayed name
 * (senderName() instead of customerName()) differ. */
class AdminMessagesFragment : CustomerMessagesFragment() {

    override val viewModel: AdminMessagesViewModel by viewModels()

    override val screenTitleRes: Int = R.string.drawer_admin_messages
    override val newMessageActionId: Int = R.id.action_admin_messages_to_new_message
    override val conversationActionId: Int = R.id.action_admin_messages_to_conversation
    override val emptyIconRes: Int = R.drawable.ic_email
    override val emptySubtitleRes: Int = R.string.admin_messages_empty_subtitle
    override fun displayName(conversation: Conversation): String = conversation.senderName()
}
