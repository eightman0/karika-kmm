package karika.distribucija.ba.salesrep.ui.messages

import karika.distribucija.ba.salesrep.model.ChatAxis

/** Mirrors composeApp's SalesAdminMessagesComponent.kt - same list/filter mechanics as
 * [CustomerMessagesViewModel], on the vendor_admin axis. */
class AdminMessagesViewModel : CustomerMessagesViewModel() {
    override val axis: ChatAxis = ChatAxis.VENDOR_ADMIN
}
