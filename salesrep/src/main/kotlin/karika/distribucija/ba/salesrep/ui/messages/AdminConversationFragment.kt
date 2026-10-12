package karika.distribucija.ba.salesrep.ui.messages

import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.model.ChatMessage

/** Mirrors composeApp's SalesAdminConversationView.kt - reuses CustomerConversationFragment's
 * bubble/input UI wholesale. Two differences from Customer's conversation screen (see
 * SalesDashboardView.kt's AdminConversation vs CustomerConversation SalesDetailTopBar wiring):
 * the action-bar title falls back to "Poruka" instead of "-", and the bubble's "other party"
 * label is the literal "Administrator" rather than the message's sender name. */
class AdminConversationFragment : CustomerConversationFragment() {

    override val viewModel: AdminConversationViewModel by viewModels()

    override val screenTitle: String
        get() = viewModel.counterpartName ?: getString(R.string.admin_conversation_default_title)

    override val senderName: (ChatMessage) -> String? = {
        getString(R.string.admin_conversation_counterpart_label)
    }
}
