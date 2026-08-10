package karika.distribucija.ba.salesrep.ui.messages

import androidx.fragment.app.viewModels
import karika.distribucija.ba.salesrep.R
import karika.distribucija.ba.salesrep.model.Message

/** Mirrors composeApp's SalesAdminConversationView.kt - reuses CustomerConversationFragment's
 * bubble/input UI wholesale. Three differences from Customer's conversation screen (see
 * SalesDashboardView.kt's AdminConversation vs CustomerConversation SalesDetailTopBar wiring):
 * the action-bar title is the conversation's subject (falling back to "Poruka"), not the
 * counterpart's name; the bubble's "other party" label is the hardcoded literal
 * "Administrator" rather than a name derived from the conversation; and "is this my message"
 * is `Message.isVendorMessage()` rather than `Message.isVendor()`. */
class AdminConversationFragment : CustomerConversationFragment() {

    override val viewModel: AdminConversationViewModel by viewModels()

    override val screenTitle: String
        get() = viewModel.subject ?: getString(R.string.admin_conversation_default_title)

    override val counterpartDisplayName: String
        get() = getString(R.string.admin_conversation_counterpart_label)

    override val isMine: (Message) -> Boolean = { it.isVendorMessage() }
}
