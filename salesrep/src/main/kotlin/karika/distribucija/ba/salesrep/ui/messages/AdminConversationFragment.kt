package karika.distribucija.ba.salesrep.ui.messages

import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R

/** Mirrors composeApp's SalesAdminConversationView.kt - reuses CustomerConversationFragment's
 * bubble/input UI wholesale. Two differences from Customer's conversation screen (see
 * SalesDashboardView.kt's AdminConversation vs CustomerConversation SalesDetailTopBar wiring):
 * the action-bar title is the conversation's subject (falling back to "Poruka"), not the
 * counterpart's name, and the bubble's "other party" label is the hardcoded literal
 * "Administrator" rather than a name derived from the conversation. */
class AdminConversationFragment : CustomerConversationFragment() {

    override val viewModel: AdminConversationViewModel by viewModels()

    override val screenTitle: String
        get() = viewModel.subject ?: getString(R.string.admin_conversation_default_title)

    override val counterpartDisplayName: String
        get() = getString(R.string.admin_conversation_counterpart_label)
}
