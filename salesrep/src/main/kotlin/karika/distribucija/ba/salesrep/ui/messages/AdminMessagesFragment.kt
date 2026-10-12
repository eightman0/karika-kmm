package karika.distribucija.ba.salesrep.ui.messages

import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R

/** Mirrors composeApp's SalesAdminMessagesView.kt - reuses CustomerMessagesFragment's list/filter
 * UI wholesale; only the title, empty-state subtitle and action ids differ. */
class AdminMessagesFragment : CustomerMessagesFragment() {

    override val viewModel: AdminMessagesViewModel by viewModels()

    override val screenTitleRes: Int = R.string.drawer_admin_messages
    override val newMessageActionId: Int = R.id.action_admin_messages_to_new_message
    override val conversationActionId: Int = R.id.action_admin_messages_to_conversation
    override val emptySubtitleRes: Int = R.string.admin_messages_empty_subtitle
}
