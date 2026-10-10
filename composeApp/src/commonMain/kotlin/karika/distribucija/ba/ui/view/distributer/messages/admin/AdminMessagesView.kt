package karika.distribucija.ba.ui.view.distributer.messages.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import karika.distribucija.ba.domain.model.ChatAxis
import karika.distribucija.ba.domain.model.ChatConversation
import karika.distribucija.ba.ui.view.distributer.messages.customer.VendorConversationList

@Composable
fun AdminMessagesView(component: AdminMessagesComponent) {
    val messages by component.messages.collectAsState()

    VendorConversationList(
        conversations = messages,
        onOpen = { component.navigateToMessagesOverview(it) },
        onNewMessage = {
            component.navigateToMessagesOverview(
                ChatConversation(
                    conversationId = null,
                    axis = ChatAxis.VENDOR_ADMIN,
                    counterpartName = "Karika Distribucija"
                )
            )
        }
    )
}
