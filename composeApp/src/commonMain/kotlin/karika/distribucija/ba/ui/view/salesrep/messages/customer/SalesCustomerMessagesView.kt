package karika.distribucija.ba.ui.view.salesrep.messages.customer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import karika.distribucija.ba.ui.view.salesrep.messages.SalesConversationList

@Composable
fun SalesCustomerMessagesView(component: SalesCustomerMessagesComponent) {
    val conversations by component.conversations.collectAsState()

    SalesConversationList(
        conversations = conversations,
        onOpen = { component.openConversation(it) },
        onNewMessage = { component.openNewMessage() },
        emptyMessage = "Ovdje će se prikazati vaše poruke sa kupcima."
    )
}
