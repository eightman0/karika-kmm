package karika.distribucija.ba.salesrep.ui.messages

import karika.distribucija.ba.salesrep.model.Conversation

/** Mirrors composeApp's SalesAdminMessagesComponent.kt/View.kt filter logic - same list/filter
 * mechanics as [CustomerMessagesViewModel], scoped to admin=true and filtered by
 * receiverId=="0" instead of sender=="vendor". */
class AdminMessagesViewModel : CustomerMessagesViewModel() {
    override val admin: Boolean = true
    override fun isSent(conversation: Conversation): Boolean = conversation.receiverId == "0"
}
